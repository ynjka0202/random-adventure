package mn.adventure;

import mn.adventure.model.Category;
import mn.adventure.model.Challenge;
import mn.adventure.model.Difficulty;
import mn.adventure.service.ChallengeGenerator;
import mn.adventure.service.ProgressService;
import mn.adventure.util.LevelSystem;
import mn.adventure.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Өгөгдлийн сангүйгээр шалгаж болох тоглоомын логикийн unit тестүүд. (mvn test) */
class GameLogicTest {

    @Test
    void levelThresholds() {
        assertEquals(1, LevelSystem.levelFor(0));
        assertEquals(1, LevelSystem.levelFor(99));
        assertEquals(2, LevelSystem.levelFor(100));
        assertEquals(3, LevelSystem.levelFor(300));
        assertEquals(5, LevelSystem.levelFor(1000));
        assertEquals(100, LevelSystem.xpToNext(0));
        assertEquals(0.5, LevelSystem.progress(50), 1e-9);
    }

    @Test
    void streakRules() {
        LocalDate today = LocalDate.of(2026, 9, 30);
        assertEquals(1, ProgressService.nextStreak(null, 0, today));
        assertEquals(4, ProgressService.nextStreak(today.minusDays(1), 3, today)); // өчигдөр → +1
        assertEquals(3, ProgressService.nextStreak(today, 3, today));              // өнөөдөр → хэвээр
        assertEquals(1, ProgressService.nextStreak(today.minusDays(3), 7, today)); // тасарсан → 1
    }

    @Test
    void xpWithStreakAndBonus() {
        Challenge medium = challenge(1, 1, Difficulty.MEDIUM);
        assertArrayEquals(new int[]{25, 0}, ProgressService.calculateXp(medium, false, 1));
        assertArrayEquals(new int[]{30, 5}, ProgressService.calculateXp(medium, false, 3));   // +20%
        assertArrayEquals(new int[]{37, 12}, ProgressService.calculateXp(medium, false, 20)); // дээд тал +50%
        assertArrayEquals(new int[]{38, 0}, ProgressService.calculateXp(medium, true, 1));    // бонус ×1.5
    }

    @Test
    void pickRespectsExclusions() {
        List<Challenge> pool = List.of(challenge(1, 1, Difficulty.EASY), challenge(2, 2, Difficulty.HARD),
                challenge(3, 3, Difficulty.MEDIUM));
        Random rnd = new Random(1);
        for (int i = 0; i < 50; i++) {
            Challenge c = ChallengeGenerator.pick(pool, Set.of(1), Set.of(2), 1, rnd).orElseThrow();
            assertEquals(3, c.getId());
        }
        assertTrue(ChallengeGenerator.pick(pool, Set.of(1, 2, 3), Set.of(), 1, rnd).isEmpty());
    }

    @Test
    void higherLevelGetsMoreHardChallenges() {
        List<Challenge> pool = List.of(challenge(1, 1, Difficulty.EASY), challenge(2, 1, Difficulty.HARD));
        int hardLow = countHard(pool, 1), hardHigh = countHard(pool, 10);
        assertTrue(hardHigh > hardLow, "Түвшин өндөр үед хэцүү сорил илүү олон гарах ёстой");
    }

    @Test
    void passwordHashing() {
        String h = PasswordUtil.hash("secret123");
        assertTrue(h.startsWith("pbkdf2$"));
        assertTrue(PasswordUtil.verify("secret123", h));
        assertFalse(PasswordUtil.verify("wrong", h));
        assertNotEquals(h, PasswordUtil.hash("secret123")); // давс өөр
    }

    private static int countHard(List<Challenge> pool, int level) {
        Random rnd = new Random(42);
        int hard = 0;
        for (int i = 0; i < 2000; i++) {
            if (ChallengeGenerator.pick(pool, Set.of(), Set.of(), level, rnd).orElseThrow().getDifficulty() == Difficulty.HARD) hard++;
        }
        return hard;
    }

    private static Challenge challenge(int id, int catId, Difficulty d) {
        Category cat = new Category(catId, "C" + catId, "Cat" + catId, "*", "#000000");
        return new Challenge(id, "Ch" + id, null, cat, d, d.getBaseXp(), null);
    }
}
