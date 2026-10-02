package mn.adventure.dao;

import mn.adventure.db.Database;
import mn.adventure.model.DailyTask;
import mn.adventure.model.TaskStatus;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** daily_tasks хүснэгт ба статистикийн асуулгууд. */
public class TaskDao {

    private static final String SELECT = """
            SELECT t.id AS t_id, t.user_id, t.assigned_date, t.status, t.is_bonus, t.completed_at, t.xp_earned,
                   c.id, c.title, c.description, c.difficulty, c.xp_reward, c.created_by,
                   cat.id AS cat_id, cat.code AS cat_code, cat.name AS cat_name, cat.icon AS cat_icon, cat.color AS cat_color
            FROM daily_tasks t
            JOIN challenges c ON c.id = t.challenge_id
            JOIN categories cat ON cat.id = c.category_id
            """;

    /** Тухайн өдрийн (солигдоогүй) даалгаврууд. */
    public List<DailyTask> findByDate(int userId, LocalDate date) throws SQLException {
        String sql = SELECT + " WHERE t.user_id = ? AND t.assigned_date = ? AND t.status <> 'SKIPPED' ORDER BY t.is_bonus, t.id";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            return list(ps);
        }
    }

    /** Бүх түүх (шинэ нь эхэнд). */
    public List<DailyTask> history(int userId) throws SQLException {
        String sql = SELECT + " WHERE t.user_id = ? ORDER BY t.assigned_date DESC, t.id DESC";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return list(ps);
        }
    }

    public void assign(int userId, int challengeId, LocalDate date, boolean bonus) throws SQLException {
        String sql = "INSERT INTO daily_tasks (user_id, challenge_id, assigned_date, is_bonus) VALUES (?,?,?,?)";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, challengeId);
            ps.setDate(3, Date.valueOf(date));
            ps.setBoolean(4, bonus);
            ps.executeUpdate();
        }
    }

    public void complete(int taskId, int xp, LocalDateTime at) throws SQLException {
        String sql = "UPDATE daily_tasks SET status='COMPLETED', completed_at=?, xp_earned=? WHERE id=? AND status='PENDING'";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(at));
            ps.setInt(2, xp);
            ps.setInt(3, taskId);
            if (ps.executeUpdate() == 0) throw new SQLException("Даалгавар аль хэдийн гүйцэтгэгдсэн байна.");
        }
    }

    public void skip(int taskId) throws SQLException {
        try (PreparedStatement ps = Database.get().connection()
                .prepareStatement("UPDATE daily_tasks SET status='SKIPPED' WHERE id=? AND status='PENDING'")) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    public int countSkipped(int userId, LocalDate date) throws SQLException {
        return scalar("SELECT COUNT(*) FROM daily_tasks WHERE user_id=? AND assigned_date=? AND status='SKIPPED'",
                userId, date);
    }

    /** Сүүлийн N өдөрт оноогдсон сорилын id-ууд (давтагдахаас сэргийлнэ). */
    public Set<Integer> recentChallengeIds(int userId, int days) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement ps = Database.get().connection().prepareStatement(
                "SELECT DISTINCT challenge_id FROM daily_tasks WHERE user_id=? AND assigned_date >= ?")) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(LocalDate.now().minusDays(days)));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) ids.add(rs.getInt(1)); }
        }
        return ids;
    }

    // ---------------- Статистик ----------------

    public int countCompleted(int userId) throws SQLException {
        return scalar("SELECT COUNT(*) FROM daily_tasks WHERE user_id=? AND status='COMPLETED'", userId, null);
    }

    public int countHardCompleted(int userId) throws SQLException {
        return scalar("""
                SELECT COUNT(*) FROM daily_tasks t JOIN challenges c ON c.id=t.challenge_id
                WHERE t.user_id=? AND t.status='COMPLETED' AND c.difficulty='HARD'""", userId, null);
    }

    public int countCategoriesTried(int userId) throws SQLException {
        return scalar("""
                SELECT COUNT(DISTINCT c.category_id) FROM daily_tasks t JOIN challenges c ON c.id=t.challenge_id
                WHERE t.user_id=? AND t.status='COMPLETED'""", userId, null);
    }

    /** Солигдоогүй нийт оноогдсон даалгавар (гүйцэтгэлийн хувь тооцоход). */
    public int countAssigned(int userId) throws SQLException {
        return scalar("SELECT COUNT(*) FROM daily_tasks WHERE user_id=? AND status<>'SKIPPED'", userId, null);
    }

    public int countActiveDays(int userId) throws SQLException {
        return scalar("SELECT COUNT(DISTINCT assigned_date) FROM daily_tasks WHERE user_id=? AND status='COMPLETED'", userId, null);
    }

    /** Өдөр бүрийн авсан XP (from-оос хойш). */
    public Map<LocalDate, Integer> xpByDay(int userId, LocalDate from) throws SQLException {
        Map<LocalDate, Integer> map = new TreeMap<>();
        try (PreparedStatement ps = Database.get().connection().prepareStatement("""
                SELECT assigned_date, SUM(xp_earned) FROM daily_tasks
                WHERE user_id=? AND status='COMPLETED' AND assigned_date >= ?
                GROUP BY assigned_date""")) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(from));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getDate(1).toLocalDate(), rs.getInt(2));
            }
        }
        return map;
    }

    /** Ангилал бүрээр гүйцэтгэсэн тоо: нэр → тоо. */
    public Map<String, Integer> completedByCategory(int userId) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement ps = Database.get().connection().prepareStatement("""
                SELECT cat.name, COUNT(t.id) FROM categories cat
                LEFT JOIN challenges c ON c.category_id = cat.id
                LEFT JOIN daily_tasks t ON t.challenge_id = c.id AND t.user_id = ? AND t.status = 'COMPLETED'
                GROUP BY cat.id, cat.name ORDER BY cat.id""")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString(1), rs.getInt(2));
            }
        }
        return map;
    }

    /** Хүндрэл бүрээр гүйцэтгэсэн тоо. */
    public Map<String, Integer> completedByDifficulty(int userId) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("EASY", 0); map.put("MEDIUM", 0); map.put("HARD", 0);
        try (PreparedStatement ps = Database.get().connection().prepareStatement("""
                SELECT c.difficulty, COUNT(*) FROM daily_tasks t JOIN challenges c ON c.id=t.challenge_id
                WHERE t.user_id=? AND t.status='COMPLETED' GROUP BY c.difficulty""")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString(1), rs.getInt(2));
            }
        }
        return map;
    }

    private int scalar(String sql, int userId, LocalDate date) throws SQLException {
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (date != null) ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private List<DailyTask> list(PreparedStatement ps) throws SQLException {
        List<DailyTask> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("completed_at");
                out.add(new DailyTask(rs.getInt("t_id"), rs.getInt("user_id"), ChallengeDao.map(rs),
                        rs.getDate("assigned_date").toLocalDate(), TaskStatus.valueOf(rs.getString("status")),
                        rs.getBoolean("is_bonus"), ts == null ? null : ts.toLocalDateTime(), rs.getInt("xp_earned")));
            }
        }
        return out;
    }
}
