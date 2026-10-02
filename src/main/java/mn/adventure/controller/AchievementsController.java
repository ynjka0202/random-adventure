package mn.adventure.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mn.adventure.model.Achievement;
import mn.adventure.service.ProgressService;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static mn.adventure.ui.UiKit.label;

/** achievements.fxml-ийн controller: нээгдсэн ба түгжигдсэн амжилтууд, ахицын хамт. */
public class AchievementsController implements PageController {
    @FXML private Label subtitleLabel;
    @FXML private ProgressBar totalBar;
    @FXML private FlowPane grid;

    @Override
    public void init(MainController main) {
        main.run(() -> {
            List<Achievement> list = new ProgressService().loadWithProgress(main.user());
            long unlocked = list.stream().filter(Achievement::isUnlocked).count();
            subtitleLabel.setText(unlocked + " / " + list.size() + " амжилт нээгдсэн");
            totalBar.setProgress(list.isEmpty() ? 0 : (double) unlocked / list.size());
            grid.getChildren().clear();
            for (Achievement a : list) grid.getChildren().add(card(a));
        });
    }

    private VBox card(Achievement a) {
        Label desc = label(a.getDescription(), "muted", "small");
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);
        desc.setStyle("-fx-text-alignment: center;");
        VBox card = new VBox(label(a.getIcon(), "ach-icon"), label(a.getTitle(), "h3"), desc);
        card.getStyleClass().addAll("card", "ach-card");
        if (a.isUnlocked()) {
            card.getChildren().addAll(
                    label("✓ " + a.getUnlockedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")), "success-text", "small"),
                    label("+" + a.getXpBonus() + " XP", "xp-chip"));
        } else {
            card.getStyleClass().add("locked");
            ProgressBar p = new ProgressBar(a.getProgressRatio());
            p.setPrefWidth(150);
            card.getChildren().addAll(p, label(Math.min(a.getProgress(), a.getThreshold()) + " / " + a.getThreshold()
                    + "  ·  +" + a.getXpBonus() + " XP", "muted", "small"));
        }
        card.setPrefHeight(230);
        return card;
    }
}
