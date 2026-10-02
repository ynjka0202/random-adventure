package mn.adventure.ui;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

import java.util.Objects;

/** Dark / Light горимыг солих. base.css + (dark.css | light.css). */
public final class Theme {
    public static final String DARK = "dark";
    public static final String LIGHT = "light";
    private static final String BASE = css("base");

    private Theme() { }

    private static String css(String name) {
        return Objects.requireNonNull(Theme.class.getResource("/css/" + name + ".css")).toExternalForm();
    }

    public static void apply(Scene scene, String theme) {
        scene.getStylesheets().setAll(css(LIGHT.equals(theme) ? LIGHT : DARK), BASE);
        scene.setUserData(LIGHT.equals(theme) ? LIGHT : DARK);
    }

    public static String current(Scene scene) {
        return LIGHT.equals(scene.getUserData()) ? LIGHT : DARK;
    }

    /** Alert/Dialog-д мөн адил стиль өгнө. */
    public static void style(DialogPane pane, Scene scene) {
        pane.getStylesheets().setAll(scene.getStylesheets());
    }
}
