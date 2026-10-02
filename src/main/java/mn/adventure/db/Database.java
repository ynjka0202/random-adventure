package mn.adventure.db;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * MySQL өгөгдлийн сантай холбогдох ганц цэг (Singleton).
 * Тохиргоог эхлээд ажиллаж буй хавтас дахь db.properties-оос,
 * байхгүй бол resources/db.properties-оос уншина.
 */
public final class Database {

    private static Database instance;
    private final String url;
    private final String user;
    private final String password;
    private Connection connection;

    private Database() {
        Properties p = new Properties();
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) { }
        Path external = Path.of("db.properties");
        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                p.load(in);
            } catch (Exception ignored) { }
        }
        this.url = p.getProperty("db.url",
                "jdbc:mysql://localhost:3306/adventure_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ulaanbaatar");
        this.user = p.getProperty("db.user", "root");
        this.password = p.getProperty("db.password", "");
    }

    public static synchronized Database get() {
        if (instance == null) instance = new Database();
        return instance;
    }

    /** Холболтыг дахин ашиглана; тасарсан бол шинээр нээнэ. */
    public synchronized Connection connection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    public String url() { return url; }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) { }
    }
}
