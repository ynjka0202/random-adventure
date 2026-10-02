package mn.adventure.dao;

import mn.adventure.db.Database;
import mn.adventure.model.User;

import java.sql.*;
import java.util.Optional;

/** users хүснэгттэй ажиллах DAO. */
public class UserDao {

    public Optional<User> findByLogin(String usernameOrEmail) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findById(int id) throws SQLException {
        try (PreparedStatement ps = Database.get().connection().prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public boolean exists(String column, String value) throws SQLException {
        if (!column.equals("username") && !column.equals("email")) throw new IllegalArgumentException(column);
        try (PreparedStatement ps = Database.get().connection()
                .prepareStatement("SELECT 1 FROM users WHERE " + column + " = ?")) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public User insert(User u) throws SQLException {
        String sql = "INSERT INTO users (username, full_name, email, password_hash, theme) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getFullName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getTheme() == null ? "dark" : u.getTheme());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
        }
        return findById(u.getId()).orElse(u);
    }

    /** XP, түвшин, streak-ийг хадгална. */
    public void updateProgress(User u) throws SQLException {
        String sql = "UPDATE users SET total_xp=?, level=?, current_streak=?, best_streak=?, last_active_date=? WHERE id=?";
        try (PreparedStatement ps = Database.get().connection().prepareStatement(sql)) {
            ps.setInt(1, u.getTotalXp());
            ps.setInt(2, u.getLevel());
            ps.setInt(3, u.getCurrentStreak());
            ps.setInt(4, u.getBestStreak());
            ps.setDate(5, u.getLastActiveDate() == null ? null : Date.valueOf(u.getLastActiveDate()));
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
    }

    public void updateTheme(int userId, String theme) throws SQLException {
        try (PreparedStatement ps = Database.get().connection().prepareStatement("UPDATE users SET theme=? WHERE id=?")) {
            ps.setString(1, theme);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setTotalXp(rs.getInt("total_xp"));
        u.setLevel(rs.getInt("level"));
        u.setCurrentStreak(rs.getInt("current_streak"));
        u.setBestStreak(rs.getInt("best_streak"));
        Date d = rs.getDate("last_active_date");
        u.setLastActiveDate(d == null ? null : d.toLocalDate());
        u.setTheme(rs.getString("theme"));
        return u;
    }
}
