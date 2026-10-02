package mn.adventure.dao;

import mn.adventure.db.Database;
import mn.adventure.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** categories хүснэгт. */
public class CategoryDao {
    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Statement st = Database.get().connection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM categories ORDER BY id")) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("code"), rs.getString("name"),
                        rs.getString("icon"), rs.getString("color")));
            }
        }
        return list;
    }
}
