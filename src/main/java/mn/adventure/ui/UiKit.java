package mn.adventure.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import mn.adventure.model.Category;
import mn.adventure.model.Difficulty;

/** Дахин ашиглагдах жижиг UI бүрэлдэхүүнүүд. */
public final class UiKit {
    private UiKit() { }

    public static Label label(String text, String... styleClasses) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styleClasses);
        return l;
    }

    public static VBox card(Node... children) {
        VBox box = new VBox(12, children);
        box.getStyleClass().add("card");
        return box;
    }

    public static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    /** Статистикийн карт: өнгөт дүрс + гарчиг + том тоо + тайлбар. */
    public static VBox statCard(String icon, String color, String title, String value, String sub) {
        Label ic = label(icon, "stat-icon");
        ic.setStyle("-fx-background-color: " + color + "33; -fx-text-fill: " + color + ";");
        Label t = label(title, "muted");
        HBox head = new HBox(12, ic, t);
        head.setAlignment(Pos.CENTER_LEFT);
        VBox card = card(head, label(value, "stat-value"), label(sub, "muted", "small"));
        card.setSpacing(6);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    public static Label categoryChip(Category c) {
        Label l = label(c.getIcon() + "  " + c.getName(), "cat-chip");
        l.setStyle("-fx-background-color: " + c.getColor() + ";");
        l.setMinWidth(Region.USE_PREF_SIZE);
        return l;
    }

    public static Label stars(Difficulty d) {
        Label l = label(d.starText(), "stars");
        l.setMinWidth(Region.USE_PREF_SIZE);
        l.setTooltip(new javafx.scene.control.Tooltip(d.getLabel()));
        return l;
    }

    public static ProgressBar xpBar(double progress, double width) {
        ProgressBar bar = new ProgressBar(progress);
        bar.getStyleClass().add("xp-bar");
        bar.setPrefWidth(width);
        bar.setMaxWidth(Double.MAX_VALUE);
        return bar;
    }

    public static StackPane avatar(String name) {
        String letter = name == null || name.isBlank() ? "?" : name.trim().substring(0, 1).toUpperCase();
        StackPane p = new StackPane(label(letter));
        p.getStyleClass().add("avatar");
        return p;
    }

    public static StackPane logo(boolean small) {
        StackPane p = new StackPane(label("⚄"));
        p.getStyleClass().add("logo-box");
        if (small) p.getStyleClass().add("small-logo");
        return p;
    }

    /** Хэсгийн гарчиг + тайлбар. */
    public static VBox pageHeader(String title, String subtitle) {
        VBox v = new VBox(4, label(title, "h1"), label(subtitle, "muted"));
        return v;
    }
}
