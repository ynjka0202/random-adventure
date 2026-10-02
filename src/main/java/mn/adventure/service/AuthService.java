package mn.adventure.service;

import mn.adventure.dao.UserDao;
import mn.adventure.model.User;
import mn.adventure.util.PasswordUtil;
import mn.adventure.util.Session;

import java.sql.SQLException;
import java.util.regex.Pattern;

/** Бүртгүүлэх, нэвтрэх логик. */
public class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,30}$");

    private final UserDao userDao;

    public AuthService() { this(new UserDao()); }
    public AuthService(UserDao userDao) { this.userDao = userDao; }

    public User register(String fullName, String username, String email, String password, String confirm)
            throws SQLException {
        fullName = fullName == null ? "" : fullName.trim();
        username = username == null ? "" : username.trim();
        email = email == null ? "" : email.trim().toLowerCase();

        if (fullName.isEmpty()) throw new ValidationException("Нэрээ оруулна уу.");
        if (!USERNAME.matcher(username).matches())
            throw new ValidationException("Хэрэглэгчийн нэр 3–30 тэмдэгт, зөвхөн латин үсэг, тоо, _ байна.");
        if (!EMAIL.matcher(email).matches()) throw new ValidationException("Имэйл хаяг буруу байна.");
        if (password == null || password.length() < 6)
            throw new ValidationException("Нууц үг хамгийн багадаа 6 тэмдэгт байна.");
        if (!password.equals(confirm)) throw new ValidationException("Нууц үг таарахгүй байна.");
        if (userDao.exists("username", username)) throw new ValidationException("Энэ хэрэглэгчийн нэр бүртгэлтэй байна.");
        if (userDao.exists("email", email)) throw new ValidationException("Энэ имэйл бүртгэлтэй байна.");

        User u = new User();
        u.setFullName(fullName);
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(PasswordUtil.hash(password));
        u.setTheme("dark");
        User saved = userDao.insert(u);
        Session.login(saved);
        return saved;
    }

    public User login(String login, String password) throws SQLException {
        if (login == null || login.isBlank() || password == null || password.isEmpty())
            throw new ValidationException("Нэвтрэх нэр болон нууц үгээ оруулна уу.");
        User u = userDao.findByLogin(login.trim().toLowerCase().contains("@") ? login.trim().toLowerCase() : login.trim())
                .orElseThrow(() -> new ValidationException("Нэвтрэх нэр эсвэл нууц үг буруу байна."));
        if (!PasswordUtil.verify(password, u.getPasswordHash()))
            throw new ValidationException("Нэвтрэх нэр эсвэл нууц үг буруу байна.");
        Session.login(u);
        return u;
    }

    public void logout() { Session.logout(); }
}
