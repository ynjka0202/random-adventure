package mn.adventure.model;

import java.util.List;

/** Сорил гүйцэтгэсний дараах үр дүн: авсан XP, түвшин ахисан эсэх, шинэ амжилтууд. */
public record CompletionResult(int xpGained, int streakBonus, int oldLevel, int newLevel,
                               int streak, List<Achievement> newAchievements) {
    public boolean leveledUp() { return newLevel > oldLevel; }
}
