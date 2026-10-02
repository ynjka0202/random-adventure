package mn.adventure.service;

import mn.adventure.dao.ChallengeDao;
import mn.adventure.dao.TaskDao;
import mn.adventure.model.Challenge;
import mn.adventure.model.DailyTask;
import mn.adventure.model.Difficulty;
import mn.adventure.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Санамсаргүй сорил үүсгэгч.
 * - Өдөрт 3 сорил, тус бүр өөр ангиллаас
 * - Сүүлийн 7 хоногт гарсан сорил давтагдахгүй (боломжтой бол)
 * - Түвшин өсөх тусам хэцүү сорил гарах магадлал нэмэгдэнэ
 * - Өдөрт 2 удаа солих (reroll), бүгдийг дуусгавал 1 бонус сорил
 */
public class ChallengeGenerator {
    public static final int DAILY_COUNT = 3;
    public static final int MAX_REROLLS = 2;
    public static final int RECENT_DAYS = 7;

    private final ChallengeDao challengeDao;
    private final TaskDao taskDao;
    private final Random random;

    public ChallengeGenerator() { this(new ChallengeDao(), new TaskDao(), new Random()); }

    public ChallengeGenerator(ChallengeDao challengeDao, TaskDao taskDao, Random random) {
        this.challengeDao = challengeDao;
        this.taskDao = taskDao;
        this.random = random;
    }

    public List<DailyTask> today(User user) throws SQLException {
        return taskDao.findByDate(user.getId(), LocalDate.now());
    }

    /** Өнөөдрийн 3 сорилыг сугална. Аль хэдийн сугалсан бол байгааг нь буцаана. */
    public List<DailyTask> rollDaily(User user) throws SQLException {
        LocalDate today = LocalDate.now();
        List<DailyTask> existing = taskDao.findByDate(user.getId(), today);
        if (!existing.isEmpty()) return existing;

        List<Challenge> pool = challengeDao.findPool(user.getId());
        Set<Integer> recent = taskDao.recentChallengeIds(user.getId(), RECENT_DAYS);
        Set<Integer> usedIds = new HashSet<>();
        Set<Integer> usedCats = new HashSet<>();
        for (int i = 0; i < DAILY_COUNT; i++) {
            Challenge ch = pick(pool, union(recent, usedIds), usedCats, user.getLevel(), random)
                    .or(() -> pick(pool, usedIds, usedCats, user.getLevel(), random))   // давталтыг зөвшөөрнө
                    .or(() -> pick(pool, usedIds, Set.of(), user.getLevel(), random))  // ангиллын хязгааргүй
                    .orElseThrow(() -> new ValidationException("Сорилын сан хоосон байна."));
            usedIds.add(ch.getId());
            usedCats.add(ch.getCategory().getId());
            taskDao.assign(user.getId(), ch.getId(), today, false);
        }
        return taskDao.findByDate(user.getId(), today);
    }

    public int rerollsLeft(User user) throws SQLException {
        return Math.max(0, MAX_REROLLS - taskDao.countSkipped(user.getId(), LocalDate.now()));
    }

    /** Нэг сорилыг өөр санамсаргүй сорилоор солино. */
    public void reroll(User user, DailyTask task) throws SQLException {
        if (task.isCompleted()) throw new ValidationException("Гүйцэтгэсэн сорилыг солих боломжгүй.");
        if (rerollsLeft(user) <= 0) throw new ValidationException("Өнөөдрийн солих эрх дууссан байна.");
        LocalDate today = LocalDate.now();
        List<DailyTask> current = taskDao.findByDate(user.getId(), today);

        Set<Integer> exclude = new HashSet<>(taskDao.recentChallengeIds(user.getId(), 0)); // өнөөдрийнх (солигдсон ч)
        Set<Integer> cats = new HashSet<>();
        for (DailyTask t : current) if (t.getId() != task.getId()) cats.add(t.getChallenge().getCategory().getId());

        List<Challenge> pool = challengeDao.findPool(user.getId());
        Set<Integer> recent = taskDao.recentChallengeIds(user.getId(), RECENT_DAYS);
        Challenge ch = pick(pool, union(exclude, recent), cats, user.getLevel(), random)
                .or(() -> pick(pool, exclude, cats, user.getLevel(), random))
                .or(() -> pick(pool, exclude, Set.of(), user.getLevel(), random))
                .orElseThrow(() -> new ValidationException("Солих өөр сорил олдсонгүй."));
        taskDao.skip(task.getId());
        taskDao.assign(user.getId(), ch.getId(), today, task.isBonus());
    }

    /** Үндсэн 3 сорил бүгд дууссан ба бонус аваагүй бол true. */
    public boolean canTakeBonus(List<DailyTask> todayTasks) {
        long mainDone = todayTasks.stream().filter(t -> !t.isBonus() && t.isCompleted()).count();
        boolean hasBonus = todayTasks.stream().anyMatch(DailyTask::isBonus);
        return mainDone >= DAILY_COUNT && !hasBonus;
    }

    /** Бонус сорил (1.5 дахин XP), хэцүү сорил гарах магадлал өндөр. */
    public void takeBonus(User user) throws SQLException {
        List<DailyTask> current = today(user);
        if (current.stream().anyMatch(DailyTask::isBonus)) throw new ValidationException("Өнөөдрийн бонус сорилоо аль хэдийн авсан байна.");
        if (!canTakeBonus(current)) throw new ValidationException("Эхлээд өнөөдрийн 3 сорилоо дуусгана уу.");
        Set<Integer> exclude = new HashSet<>(taskDao.recentChallengeIds(user.getId(), 0));
        List<Challenge> pool = challengeDao.findPool(user.getId());
        Challenge ch = pick(pool, exclude, Set.of(), user.getLevel() + 6, random)
                .orElseThrow(() -> new ValidationException("Бонус сорил олдсонгүй."));
        taskDao.assign(user.getId(), ch.getId(), LocalDate.now(), true);
    }

    /**
     * Жинлэсэн санамсаргүй сонголт (weighted random).
     * Хүндрэлийн жин түвшнээс хамаарна: түвшин өсөхөд EASY-ийн жин буурч, HARD-ийн жин өснө.
     */
    public static Optional<Challenge> pick(List<Challenge> pool, Set<Integer> excludeIds,
                                           Set<Integer> excludeCategories, int level, Random random) {
        List<Challenge> candidates = pool.stream()
                .filter(c -> !excludeIds.contains(c.getId()))
                .filter(c -> !excludeCategories.contains(c.getCategory().getId()))
                .toList();
        if (candidates.isEmpty()) return Optional.empty();

        double total = 0;
        double[] weights = new double[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            weights[i] = weight(candidates.get(i).getDifficulty(), level);
            total += weights[i];
        }
        double r = random.nextDouble() * total;
        for (int i = 0; i < candidates.size(); i++) {
            r -= weights[i];
            if (r < 0) return Optional.of(candidates.get(i));
        }
        return Optional.of(candidates.get(candidates.size() - 1));
    }

    static double weight(Difficulty d, int level) {
        return switch (d) {
            case EASY -> Math.max(1.0, 6.0 - level * 0.5);
            case MEDIUM -> 4.0;
            case HARD -> Math.min(6.0, 1.0 + level * 0.5);
        };
    }

    private static Set<Integer> union(Set<Integer> a, Set<Integer> b) {
        Set<Integer> s = new HashSet<>(a);
        s.addAll(b);
        return s;
    }
}
