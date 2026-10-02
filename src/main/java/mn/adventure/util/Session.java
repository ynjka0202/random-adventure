package mn.adventure.util;

import mn.adventure.model.User;

/** Нэвтэрсэн хэрэглэгчийг програмын турш хадгална. */
public final class Session {
    private static User currentUser;

    private Session() { }

    public static User user() { return currentUser; }
    public static void login(User user) { currentUser = user; }
    public static void logout() { currentUser = null; }
    public static boolean isLoggedIn() { return currentUser != null; }
}
