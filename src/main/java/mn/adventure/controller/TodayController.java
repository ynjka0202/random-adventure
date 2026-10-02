package mn.adventure.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.util.Duration;
import mn.adventure.model.CompletionResult;
import mn.adventure.model.DailyTask;
import mn.adventure.service.ChallengeGenerator;
import mn.adventure.service.ProgressService;
import mn.adventure.ui.UiKit;

import java.util.List;
import java.util.Random;

import static mn.adventure.ui.UiKit.label;

/**
 * today.fxml-ийн controller: сорил сугалах (шооны анимац), гүйцэтгэх, солих, бонус авах.
 * Хуудасны бүтэц FXML-д, сорилын картууд өгөгдлөөс хамаарч кодоор үүснэ.
 */
public class TodayController implements PageController {
    private static final String[] DICE = {"⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};

    @FXML private Label subtitleLabel;
    @FXML private ProgressBar progressBar;
    @FXML private VBox rollPanel;
    @FXML private Label diceLabel;
    @FXML private Button rollButton;
    @FXML private TilePane taskGrid;
    @FXML private VBox bonusPanel;
    @FXML private VBox donePanel;

    private final ChallengeGenerator generator = new ChallengeGenerator();
    private final ProgressService progress = new ProgressService();
    private MainController main;

    @Override
    public void init(MainController main) {
        this.main = main;
        draw(false);
    }

    /** Өнөөдрийн төлвөөс хамаарч аль хэсгийг харуулахыг шийдэж, картуудыг зурна. */
    private void draw(boolean animate) {
        List<DailyTask> tasks;
        int rerolls;
        try {
            tasks = generator.today(main.user());
            rerolls = generator.rerollsLeft(main.user());
        } catch (Exception e) {
            subtitleLabel.setText("Алдаа: " + e.getMessage());
            return;
        }
        long done = tasks.stream().filter(DailyTask::isCompleted).count();
        boolean empty = tasks.isEmpty();

        show(rollPanel, empty);
        show(progressBar, !empty);
        show(taskGrid, !empty);
        show(bonusPanel, generator.canTakeBonus(tasks));
        show(donePanel, !empty && done == tasks.size() && !generator.canTakeBonus(tasks));

        if (empty) {
            subtitleLabel.setText("Шоогоо хаяж өнөөдрийн 3 адал явдлаа сугална уу.");
            return;
        }
        subtitleLabel.setText(done + " / " + tasks.size() + " гүйцэтгэсэн · Солих эрх: " + rerolls + " үлдсэн");
        progressBar.setProgress((double) done / tasks.size());
        taskGrid.getChildren().clear();
        int i = 0;
        for (DailyTask t : tasks) {
            VBox card = taskCard(t, rerolls);
            taskGrid.getChildren().add(card);
            if (animate) animateIn(card, i++);
        }
    }

    private static void show(Node n, boolean visible) {
        n.setVisible(visible);
        n.setManaged(visible);
    }

    /** «Сорилоо сугалах» товч: шоо эргэлдэх анимацын дараа 3 сорил сугална. */
    @FXML
    private void roll() {
        rollButton.setDisable(true);
        Random rnd = new Random();
        Timeline faces = new Timeline(new KeyFrame(Duration.millis(80),
                ev -> diceLabel.setText(DICE[rnd.nextInt(DICE.length)])));
        faces.setCycleCount(12);
        RotateTransition spin = new RotateTransition(Duration.millis(960), diceLabel);
        spin.setByAngle(720);
        ParallelTransition anim = new ParallelTransition(faces, spin);
        anim.setOnFinished(ev -> {
            if (main.run(() -> generator.rollDaily(main.user()))) {
                main.toast("Өнөөдрийн 3 сорил бэлэн боллоо!", false);
                draw(true);
            } else {
                rollButton.setDisable(false);
            }
        });
        anim.play();
    }

    @FXML
    private void takeBonus() {
        if (main.run(() -> generator.takeBonus(main.user()))) {
            main.toast("Бонус сорил нэмэгдлээ — 1.5 дахин XP!", false);
            draw(true);
        }
    }

    // ---------------- Сорилын карт ----------------
    private VBox taskCard(DailyTask t, int rerolls) {
        var ch = t.getChallenge();
        Region stripe = new Region();
        stripe.getStyleClass().add("task-stripe");
        stripe.setStyle("-fx-background-color: " + ch.getCategory().getColor() + ";");

        HBox chips = new HBox(8, UiKit.categoryChip(ch.getCategory()));
        if (t.isBonus()) chips.getChildren().add(label("БОНУС ×1.5", "bonus-chip"));
        chips.getChildren().addAll(UiKit.spacer(), UiKit.stars(ch.getDifficulty()));
        chips.setAlignment(Pos.CENTER_LEFT);

        Label title = label(ch.getTitle(), "task-title");
        title.setWrapText(true);
        title.setMinHeight(Region.USE_PREF_SIZE);
        Label desc = label(ch.getDescription() == null ? "" : ch.getDescription(), "muted");
        desc.setWrapText(true);
        desc.setMinHeight(40);

        int xp = t.isBonus() ? (int) Math.round(ch.getXpReward() * ProgressService.BONUS_TASK_MULTIPLIER) : ch.getXpReward();
        HBox meta = new HBox(8, label("+" + xp + " XP", "xp-chip"), label(ch.getDifficulty().getLabel(), "muted", "small"));
        if (ch.isCustom()) meta.getChildren().add(label("· Миний сорил", "muted", "small"));
        meta.setAlignment(Pos.CENTER_LEFT);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        if (t.isCompleted()) {
            actions.getChildren().add(label("✓ Гүйцэтгэсэн · +" + t.getXpEarned() + " XP", "success-text", "h3"));
        } else {
            Button doneBtn = new Button("✓  Хийлээ");
            doneBtn.getStyleClass().add("btn-primary");
            doneBtn.setOnAction(e -> complete(t));
            Button reroll = new Button("↻  Солих");
            reroll.getStyleClass().add("btn-ghost");
            reroll.setDisable(rerolls <= 0);
            reroll.setOnAction(e -> {
                if (main.run(() -> generator.reroll(main.user(), t))) {
                    main.toast("Сорил солигдлоо ↻", false);
                    draw(true);
                }
            });
            actions.getChildren().addAll(doneBtn, reroll);
        }

        VBox body = new VBox(10, chips, title, desc, meta, UiKit.spacer(), actions);
        body.getStyleClass().add("task-body");
        VBox card = new VBox(stripe, body);
        card.getStyleClass().addAll("card", "task-card");
        if (t.isCompleted()) card.getStyleClass().add("done");
        card.setPrefHeight(270);
        VBox.setVgrow(body, Priority.ALWAYS);
        return card;
    }

    private void complete(DailyTask t) {
        CompletionResult[] result = new CompletionResult[1];
        if (main.run(() -> result[0] = progress.complete(main.user(), t))) {
            main.refreshHeader();
            main.showReward(result[0], () -> draw(false));
            draw(false);
        }
    }

    private void animateIn(Node n, int index) {
        n.setOpacity(0);
        n.setTranslateY(24);
        FadeTransition f = new FadeTransition(Duration.millis(350), n);
        f.setToValue(1);
        TranslateTransition tr = new TranslateTransition(Duration.millis(350), n);
        tr.setToY(0);
        ParallelTransition p = new ParallelTransition(f, tr);
        p.setDelay(Duration.millis(120L * index));
        p.play();
    }
}
