package mn.adventure.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Тухайн өдөр хэрэглэгчид санамсаргүйгээр оноогдсон сорил. */
public class DailyTask {
    private int id;
    private int userId;
    private Challenge challenge;
    private LocalDate assignedDate;
    private TaskStatus status;
    private boolean bonus;
    private LocalDateTime completedAt;
    private int xpEarned;

    public DailyTask(int id, int userId, Challenge challenge, LocalDate assignedDate, TaskStatus status,
                     boolean bonus, LocalDateTime completedAt, int xpEarned) {
        this.id = id;
        this.userId = userId;
        this.challenge = challenge;
        this.assignedDate = assignedDate;
        this.status = status;
        this.bonus = bonus;
        this.completedAt = completedAt;
        this.xpEarned = xpEarned;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public Challenge getChallenge() { return challenge; }
    public LocalDate getAssignedDate() { return assignedDate; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public boolean isBonus() { return bonus; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public int getXpEarned() { return xpEarned; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }
    public boolean isCompleted() { return status == TaskStatus.COMPLETED; }
}
