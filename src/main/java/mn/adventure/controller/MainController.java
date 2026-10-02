package mn.adventure.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import mn.adventure.App;
import mn.adventure.dao.UserDao;
import mn.adventure.model.Achievement;
import mn.adventure.model.CompletionResult;
import mn.adventure.model.User;
import mn.adventure.service.ValidationException;
import mn.adventure.ui.Theme;
import mn.adventure.util.LevelSystem;
import mn.adventure.util.Session;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static mn.adventure.ui.UiKit.label;

/**
 * main.fxml-ийн controller. Цэс, дээд мөр (тоглогчийн мэдээлэл), хуудас солих,
 * toast болон шагналын popup-ийг удирдана. Хуудас бүр өөрийн FXML + controller-тэй.
 */
public class MainController {

    /** Цэсний хуудас ба түүний FXML файл. */
    public enum PageKey {
        DASHBOARD("dashboard.fxml"), TODAY("today.fxml"), ACHIEVEMENTS("achievements.fxml"),
        STATS("stats.fxml"), HISTORY("history.fxml"), CUSTOM("custom.fxml");

        final String fxml;
        PageKey(String fxml) { this.fxml = fxml; }
    }

    /** SQLException шиддэг үйлдэл. */
    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }

    @FXML private StackPane root;
    @FXML private StackPane overlay;
    @FXML private VBox toasts;
    @FXML private ScrollPane scroll;
    @FXML private ToggleButton navDashboard, navToday, navAchievements, navStats, navHistory, navCustom;
    @FXML private Button themeButton;
    @FXML private Label greetingLabel, dateLabel, streakLabel, avatarLabel, nameLabel, levelLabel, xpLabel;
    @FXML private ProgressBar xpBar;

    private Map<PageKey, ToggleButton> navButtons;
    private PageKey current = PageKey.DASHBOARD;

    @FXML
    private void initialize() {
        navButtons = Map.of(PageKey.DASHBOARD, navDashboard, PageKey.TODAY, navToday,
                PageKey.ACHIEVEMENTS, navAchievements, PageKey.STATS, navStats,
                PageKey.HISTORY, navHistory, PageKey.CUSTOM, navCustom);
        // ToggleButton-ийн анхны "toggle-button" загварыг арилгаж зөвхөн цэсний загварыг үлдээнэ
        navButtons.values().forEach(b -> b.getStyleClass().setAll("nav-button"));
        updateThemeText();
        refreshHeader();
        navigate(PageKey.DASHBOARD);
    }

    public User user() { return Session.user(); }

    // ---------------- Цэсний товчнууд ----------------
    @FXML private void goDashboard() { navigate(PageKey.DASHBOARD); }
    @FXML private void goToday() { navigate(PageKey.TODAY); }
    @FXML private void goAchievements() { navigate(PageKey.ACHIEVEMENTS); }
    @FXML private void goStats() { navigate(PageKey.STATS); }
    @FXML private void goHistory() { navigate(PageKey.HISTORY); }
    @FXML private void goCustom() { navigate(PageKey.CUSTOM); }
    @FXML private void logout() { App.showAuth(); }

    @FXML
    private void toggleTheme() {
        String next = Theme.DARK.equals(Theme.current(App.scene())) ? Theme.LIGHT : Theme.DARK;
        Theme.apply(App.scene(), next);
        user().setTheme(next);
        updateThemeText();
        run(() -> new UserDao().updateTheme(user().getId(), next));
        navigate(current); // графикийн өнгийг шинэчлэх
    }

    private void updateThemeText() {
        themeButton.setText(Theme.DARK.equals(Theme.current(App.scene())) ? "☀   Light горим" : "☾   Dark горим");
    }

    /** Хуудасны FXML-ийг ачаалж, controller-т MainController-ийг дамжуулаад төв хэсэгт харуулна. */
    public void navigate(PageKey key) {
        current = key;
        navButtons.get(key).setSelected(true);
        Node content;
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + key.fxml));
            content = loader.load();
            PageController controller = loader.getController();
            controller.init(this);
        } catch (Exception e) {
            e.printStackTrace();
            content = label("Алдаа гарлаа: " + e.getMessage(), "error-text");
        }
        VBox wrap = new VBox(content);
        wrap.getStyleClass().add("content");
        scroll.setContent(wrap);
        scroll.setVvalue(0);
        FadeTransition ft = new FadeTransition(Duration.millis(220), wrap);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    public void reload() { navigate(current); }

    // ---------------- Дээд мөр ----------------
    public void refreshHeader() {
        User u = user();
        LocalDate today = LocalDate.now();
        greetingLabel.setText("Сайн уу, " + u.getFullName() + "!");
        dateLabel.setText(today.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " · " + weekday(today));

        int streak = u.getEffectiveStreak(today);
        streakLabel.setText("♨  " + streak + " өдөр");
        streakLabel.setTooltip(new Tooltip("Дараалсан идэвхтэй өдөр. Шилдэг: " + u.getBestStreak()));
        streakLabel.setStyle(streak > 0 ? "-fx-text-fill: -c-gold;" : "");

        String name = u.getFullName() == null || u.getFullName().isBlank() ? "?" : u.getFullName().trim();
        avatarLabel.setText(name.substring(0, 1).toUpperCase());
        nameLabel.setText(u.getFullName());
        int lvl = u.getLevel();
        int start = LevelSystem.xpForLevel(lvl);
        int end = LevelSystem.xpForLevel(lvl + 1);
        levelLabel.setText("Lv " + lvl);
        xpBar.setProgress(LevelSystem.progress(u.getTotalXp()));
        xpLabel.setText((u.getTotalXp() - start) + " / " + (end - start) + " XP");
    }

    private static String weekday(LocalDate d) {
        return switch (d.getDayOfWeek()) {
            case MONDAY -> "Даваа";
            case TUESDAY -> "Мягмар";
            case WEDNESDAY -> "Лхагва";
            case THURSDAY -> "Пүрэв";
            case FRIDAY -> "Баасан";
            case SATURDAY -> "Бямба";
            case SUNDAY -> "Ням";
        };
    }

    // ---------------- Алдааг toast-оор харуулах ----------------
    public boolean run(Action action) {
        try {
            action.run();
            return true;
        } catch (ValidationException e) {
            toast(e.getMessage(), true);
        } catch (Exception e) {
            toast("Алдаа: " + e.getMessage(), true);
        }
        return false;
    }

    public void toast(String message, boolean error) {
        HBox t = new HBox(label(message));
        t.getStyleClass().add("toast");
        if (error) t.getStyleClass().add("error");
        t.setMaxWidth(380);
        toasts.getChildren().add(t);
        t.setOpacity(0);
        FadeTransition in = new FadeTransition(Duration.millis(200), t);
        in.setToValue(1);
        PauseTransition wait = new PauseTransition(Duration.seconds(3));
        FadeTransition out = new FadeTransition(Duration.millis(300), t);
        out.setToValue(0);
        SequentialTransition seq = new SequentialTransition(in, wait, out);
        seq.setOnFinished(e -> toasts.getChildren().remove(t));
        seq.play();
    }

    // ---------------- Шагналын popup ----------------
    public void showReward(CompletionResult r, Runnable onClose) {
        VBox card = new VBox();
        card.getStyleClass().add("reward-card");
        card.setMaxSize(440, Region.USE_PREF_SIZE);

        card.getChildren().add(label(r.leveledUp() ? "▲ LEVEL UP!" : "✓ Сорил биеллээ!", "reward-title"));
        Label xp = label("+" + r.xpGained() + " XP", "reward-xp");
        card.getChildren().add(xp);
        if (r.streakBonus() > 0) {
            card.getChildren().add(label("♨ " + r.streak() + " өдрийн streak бонус: +" + r.streakBonus() + " XP", "gold"));
        } else {
            card.getChildren().add(label("♨ Streak: " + r.streak() + " өдөр", "muted"));
        }
        if (r.leveledUp()) {
            card.getChildren().add(label("Та " + r.newLevel() + "-р түвшинд хүрлээ — " + LevelSystem.title(r.newLevel()), "h3"));
        }
        int shown = 0;
        for (Achievement a : r.newAchievements()) {
            if (shown++ == 3) {
                card.getChildren().add(label("+ " + (r.newAchievements().size() - 3) + " өөр амжилт нээгдлээ! (Амжилт цэснээс харна уу)", "gold"));
                break;
            }
            Label ic = label(a.getIcon(), "ach-icon");
            ic.setScaleX(0.7);
            ic.setScaleY(0.7);
            VBox txt = new VBox(2, label("Шинэ амжилт: " + a.getTitle(), "h3"),
                    label(a.getDescription() + "  (+" + a.getXpBonus() + " XP)", "muted", "small"));
            HBox row = new HBox(8, ic, txt);
            row.setAlignment(Pos.CENTER_LEFT);
            card.getChildren().add(row);
        }
        Button ok = new Button("Гайхалтай!");
        ok.getStyleClass().addAll("btn-gold", "btn-lg");
        ok.setDefaultButton(true);
        VBox.setMargin(ok, new Insets(8, 0, 0, 0));
        card.getChildren().add(ok);

        Region dim = new Region();
        dim.getStyleClass().add("overlay-dim");
        StackPane layer = new StackPane(dim, card);
        overlay.getChildren().setAll(layer);
        overlay.setPickOnBounds(true);

        Runnable close = () -> {
            FadeTransition out = new FadeTransition(Duration.millis(180), layer);
            out.setToValue(0);
            out.setOnFinished(e -> {
                overlay.getChildren().clear();
                overlay.setPickOnBounds(false);
                if (onClose != null) onClose.run();
            });
            out.play();
        };
        ok.setOnAction(e -> close.run());
        dim.setOnMouseClicked(e -> close.run());

        // Орох анимац: том болж гарч ирээд XP тоо өснө
        layer.setOpacity(0);
        card.setScaleX(0.6);
        card.setScaleY(0.6);
        FadeTransition fade = new FadeTransition(Duration.millis(200), layer);
        fade.setToValue(1);
        ScaleTransition grow = new ScaleTransition(Duration.millis(260), card);
        grow.setToX(1.08);
        grow.setToY(1.08);
        grow.setInterpolator(Interpolator.EASE_OUT);
        ScaleTransition settle = new ScaleTransition(Duration.millis(140), card);
        settle.setToX(1);
        settle.setToY(1);
        SequentialTransition pop = new SequentialTransition(grow, settle);
        Timeline count = new Timeline();
        for (int i = 0; i <= 20; i++) {
            int v = (int) Math.round(r.xpGained() * i / 20.0);
            count.getKeyFrames().add(new KeyFrame(Duration.millis(200 + i * 30), e -> xp.setText("+" + v + " XP")));
        }
        new ParallelTransition(fade, pop, count).play();
        ok.requestFocus();
    }
}
