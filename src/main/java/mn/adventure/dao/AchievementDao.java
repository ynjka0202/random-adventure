package mn.adventure.dao;

import mn.adventure.db.Database;
import mn.adventure.model.Achievement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** achievements ба user_achievements хүснэгт. */
public class AchievementDao {

    /** Бүх амжилтыг тухайн хэрэглэгч нээсэн эсэхийн хамт буцаана. */
    public List<Achievement> findAllForUser(int userId) throws SQLException {
        String sql = """
                SELECT a.*, ua.unlocked_at FROM achievements a
                LEFT JOIN user_achievements ua ON ua.achievement_id = a.id AND ua.user_id = ?
                ORDER BY a.id""";
        List<Achievement> list = new ArrayList<>();
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Achievement a = new Achievement(rs.getInt("id"), rs.getString("code"), rs.getString("title"),
                            rs.getString("description"), rs.getString("icon"),
                            Achievement.ConditionType.valueOf(rs.getString("condition_type")),
                            rs.getInt("threshold"), rs.getInt("xp_bonus"));
                    Timestamp ts = rs.getTimestamp("unlocked_at");
                    if (ts != null) a.setUnlockedAt(ts.toLocalDateTime());
                    list.add(a);
                }
            }
        }
        return list;
    }

    public void unlock(int userId, int achievementId) throws SQLException {
        try (PreparedStatement ps = Database.get().connection().prepareStatement(
                "INSERT IGNORE INTO user_achievements (user_id, achievement_id) VALUES (?, ?)")) {
            ps.setInt(1, userId);
            ps.setInt(2, achievementId);
            ps.executeUpdate();
        }
    }
}
