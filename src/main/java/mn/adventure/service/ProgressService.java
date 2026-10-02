package mn.adventure.service;

import mn.adventure.dao.AchievementDao;
import mn.adventure.dao.TaskDao;
import mn.adventure.dao.UserDao;
import mn.adventure.db.Database;
import mn.adventure.model.*;
import mn.adventure.util.LevelSystem;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Урамшууллын систем: XP, түвшин, streak (дараалсан өдөр), амжилт.
 */
public class ProgressService {
    /** Streak өдөр бүрт +10% XP, дээд тал нь +50%. */
    public static final int STREAK_BONUS_PERCENT = 10;
    public static final int STREAK_BONUS_MAX_DAYS = 5;
    public static final double BONUS_TASK_MULTIPLIER = 1.5;

    private final TaskDao taskDao;
    private final UserDao userDao;
    private final AchievementDao achievementDao;

    public ProgressService() { this(new TaskDao(), new UserDao(), new AchievementDao()); }

    public ProgressService(TaskDao taskDao, UserDao userDao, AchievementDao achievementDao) {
        this.taskDao = taskDao;
        this.userDao = userDao;
        this.achievementDao = achievementDao;
    }

    /** Streak-ийг шинэчилнэ: өчигдөр идэвхтэй байсан бол +1, өнөөдөр аль хэдийн бол хэвээр, бусад үед 1. */
    public static int nextStreak(LocalDate lastActive, int currentStreak, LocalDate today) {
        if (lastActive == null) return 1;
        if (lastActive.equals(today)) return Math.max(1, currentStreak);
        if (lastActive.equals(today.minusDays(1))) return currentStreak + 1;
        return 1;
    }

    /** Сорилын XP: үндсэн + бонус сорилын үржүүлэгч + streak бонус. */
    public static int[] calculateXp(Challenge ch, boolean bonusTask, int streak) {
        int base = ch.getXpReward();
        if (bonusTask) base = (int) Math.round(base * BONUS_TASK_MULTIPLIER);
        int streakDays = Math.min(Math.max(streak - 1, 0), STREAK_BONUS_MAX_DAYS);
        int streakBonus = base * streakDays * STREAK_BONUS_PERCENT / 100;
        return new int[]{base + streakBonus, streakBonus};
    }

    /** Сорилыг гүйцэтгэсэн гэж тэмдэглээд урамшууллыг тооцно (нэг transaction дотор). */
    public CompletionResult complete(User user, DailyTask task) throws SQLException {
        LocalDate today = LocalDate.now();
        if (task.isCompleted()) throw new ValidationException("Энэ сорил аль хэдийн гүйцэтгэгдсэн.");
        if (!task.getAssignedDate().equals(today)) throw new ValidationException("Зөвхөн өнөөдрийн сорилыг тэмдэглэнэ.");

        Connection con = Database.get().connection();
        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            int oldLevel = user.getLevel();
            int streak = nextStreak(user.getLastActiveDate(), user.getCurrentStreak(), today);
            int[] xp = calculateXp(task.getChallenge(), task.isBonus(), streak);

            taskDao.complete(task.getId(), xp[0], LocalDateTime.now());
            user.setCurrentStreak(streak);
            user.setBestStreak(Math.max(user.getBestStreak(), streak));
            user.setLastActiveDate(today);
            user.setTotalXp(user.getTotalXp() + xp[0]);
            user.setLevel(LevelSystem.levelFor(user.getTotalXp()));

            List<Achievement> unlocked = checkAchievements(user);
            userDao.updateProgress(user);
            con.commit();

            task.setStatus(TaskStatus.COMPLETED);
            task.setXpEarned(xp[0]);
            return new CompletionResult(xp[0], xp[1], oldLevel, user.getLevel(), streak, unlocked);
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            // Санах ой дахь хэрэглэгчийн өгөгдлийг өгөгдлийн сантай тааруулна
            userDao.findById(user.getId()).ifPresent(fresh -> {
                user.setTotalXp(fresh.getTotalXp());
                user.setLevel(fresh.getLevel());
                user.setCurrentStreak(fresh.getCurrentStreak());
                user.setBestStreak(fresh.getBestStreak());
                user.setLastActiveDate(fresh.getLastActiveDate());
            });
            throw e;
        } finally {
            con.setAutoCommit(oldAuto);
        }
    }

    /** Нөхцөл хангасан амжилтуудыг нээж, бонус XP нэмнэ. Бонус XP-ээр шинэ амжилт нээгдэж болох тул давтана. */
    private List<Achievement> checkAchievements(User user) throws SQLException {
        List<Achievement> newly = new ArrayList<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Achievement a : loadWithProgress(user)) {
                if (!a.isUnlocked() && a.getProgress() >= a.getThreshold()) {
                    achievementDao.unlock(user.getId(), a.getId());
                    user.setTotalXp(user.getTotalXp() + a.getXpBonus());
                    user.setLevel(LevelSystem.levelFor(user.getTotalXp()));
                    newly.add(a);
                    changed = true;
                }
            }
        }
        return newly;
    }

    /** Бүх амжилтыг хэрэглэгчийн одоогийн ахицын хамт. */
    public List<Achievement> loadWithProgress(User user) throws SQLException {
        List<Achievement> list = achievementDao.findAllForUser(user.getId());
        int completed = taskDao.countCompleted(user.getId());
        int hard = taskDao.countHardCompleted(user.getId());
        int cats = taskDao.countCategoriesTried(user.getId());
        for (Achievement a : list) {
            a.setProgress(switch (a.getConditionType()) {
                case TASKS_COMPLETED -> completed;
                case STREAK -> user.getBestStreak();
                case LEVEL -> user.getLevel();
                case TOTAL_XP -> user.getTotalXp();
                case HARD_COMPLETED -> hard;
                case CATEGORIES_TRIED -> cats;
            });
        }
        return list;
    }
}
