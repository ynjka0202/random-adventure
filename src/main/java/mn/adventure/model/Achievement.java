package mn.adventure.model;

import java.time.LocalDateTime;

/** Амжилт (badge). progress нь тухайн хэрэглэгчийн одоогийн утга. */
public class Achievement {
    public enum ConditionType { TASKS_COMPLETED, STREAK, LEVEL, TOTAL_XP, HARD_COMPLETED, CATEGORIES_TRIED }

    private final int id;
    private final String code;
    private final String title;
    private final String description;
    private final String icon;
    private final ConditionType conditionType;
    private final int threshold;
    private final int xpBonus;
    private LocalDateTime unlockedAt;
    private int progress;

    public Achievement(int id, String code, String title, String description, String icon,
                       ConditionType conditionType, int threshold, int xpBonus) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.conditionType = conditionType;
        this.threshold = threshold;
        this.xpBonus = xpBonus;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public ConditionType getConditionType() { return conditionType; }
    public int getThreshold() { return threshold; }
    public int getXpBonus() { return xpBonus; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
    public boolean isUnlocked() { return unlockedAt != null; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public double getProgressRatio() { return Math.min(1.0, threshold == 0 ? 1 : (double) progress / threshold); }
}
