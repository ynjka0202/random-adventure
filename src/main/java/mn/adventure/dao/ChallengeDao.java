package mn.adventure.dao;

import mn.adventure.db.Database;
import mn.adventure.model.Category;
import mn.adventure.model.Challenge;
import mn.adventure.model.Difficulty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** challenges хүснэгт (сорилын сан). */
public class ChallengeDao {

    static final String SELECT = """
            SELECT c.id, c.title, c.description, c.difficulty, c.xp_reward, c.created_by,
                   cat.id AS cat_id, cat.code AS cat_code, cat.name AS cat_name, cat.icon AS cat_icon, cat.color AS cat_color
            FROM challenges c JOIN categories cat ON cat.id = c.category_id
            """;

    /** Хэрэглэгчид санамсаргүй сонгож болох бүх идэвхтэй сорил: системийнх + өөрийнх. */
    public List<Challenge> findPool(int userId) throws SQLException {
        String sql = SELECT + " WHERE c.is_active = TRUE AND (c.created_by IS NULL OR c.created_by = ?)";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return list(ps);
        }
    }

    /** Хэрэглэгчийн өөрөө нэмсэн сорилууд. */
    public List<Challenge> findCustom(int userId) throws SQLException {
        String sql = SELECT + " WHERE c.is_active = TRUE AND c.created_by = ? ORDER BY c.created_at DESC";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return list(ps);
        }
    }

    public void insert(Challenge ch) throws SQLException {
        String sql = "INSERT INTO challenges (title, description, category_id, difficulty, xp_reward, created_by) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ch.getTitle());
            ps.setString(2, ch.getDescription());
            ps.setInt(3, ch.getCategory().getId());
            ps.setString(4, ch.getDifficulty().name());
            ps.setInt(5, ch.getXpReward());
            ps.setInt(6, ch.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) ch.setId(k.getInt(1)); }
        }
    }

    /** Түүхэнд холбоос нь үлдэхийн тулд устгахгүй, идэвхгүй болгоно. */
    public void deactivate(int challengeId, int userId) throws SQLException {
        try (PreparedStatement ps = Database.get().connection()
                .prepareStatement("UPDATE challenges SET is_active = FALSE WHERE id = ? AND created_by = ?")) {
            ps.setInt(1, challengeId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private List<Challenge> list(PreparedStatement ps) throws SQLException {
        List<Challenge> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    static Challenge map(ResultSet rs) throws SQLException {
        int createdByValue = rs.getInt("created_by");
        Integer createdBy = rs.wasNull() ? null : createdByValue; // NULL = системийн сорил
        Category cat = new Category(rs.getInt("cat_id"), rs.getString("cat_code"), rs.getString("cat_name"),
                rs.getString("cat_icon"), rs.getString("cat_color"));
        return new Challenge(rs.getInt("id"), rs.getString("title"), rs.getString("description"), cat,
                Difficulty.valueOf(rs.getString("difficulty")), rs.getInt("xp_reward"), createdBy);
    }
}
