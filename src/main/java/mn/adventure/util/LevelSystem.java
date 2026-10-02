package mn.adventure.util;

/**
 * Түвшний тооцоо. n-р түвшинд хүрэхэд нийт 50·n·(n−1) XP хэрэгтэй:
 * Lv2 = 100, Lv3 = 300, Lv4 = 600, Lv5 = 1000 ...
 */
public final class LevelSystem {
    private LevelSystem() { }

    /** n-р түвшинд хүрэхэд шаардлагатай нийт XP. */
    public static int xpForLevel(int level) {
        return 50 * level * (level - 1);
    }

    /** Нийт XP-ээс түвшинг олно. */
    public static int levelFor(int totalXp) {
        int level = 1;
        while (xpForLevel(level + 1) <= totalXp) level++;
        return level;
    }

    /** Одоогийн түвшин доторх ахиц (0.0 – 1.0). */
    public static double progress(int totalXp) {
        int level = levelFor(totalXp);
        int start = xpForLevel(level);
        int end = xpForLevel(level + 1);
        return (double) (totalXp - start) / (end - start);
    }

    /** Дараагийн түвшин хүртэл үлдсэн XP. */
    public static int xpToNext(int totalXp) {
        return xpForLevel(levelFor(totalXp) + 1) - totalXp;
    }

    /** Түвшний цол. */
    public static String title(int level) {
        if (level >= 15) return "Домогт";
        if (level >= 10) return "Баатар";
        if (level >= 7) return "Эрэлхэг";
        if (level >= 5) return "Судлаач";
        if (level >= 3) return "Аялагч";
        return "Шинэ адал явдалт";
    }
}
