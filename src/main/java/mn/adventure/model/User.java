package mn.adventure.model;

import java.time.LocalDate;

/** Системийн хэрэглэгч (тоглогч). */
public class User {
    private int id;
    private String username;
    private String fullName;
    private String email;
    private String passwordHash;
    private int totalXp;
    private int level;
    private int currentStreak;
    private int bestStreak;
    private LocalDate lastActiveDate;
    private String theme;

    public User() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }
    public LocalDate getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(LocalDate lastActiveDate) { this.lastActiveDate = lastActiveDate; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    /** Өчигдрөөс хойш идэвхгүй бол streak тасарсан гэж үзнэ. */
    public int getEffectiveStreak(LocalDate today) {
        if (lastActiveDate == null) return 0;
        return lastActiveDate.isBefore(today.minusDays(1)) ? 0 : currentStreak;
    }
}
