package mn.adventure.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mn.adventure.dao.TaskDao;
import mn.adventure.model.Achievement;
import mn.adventure.model.DailyTask;
import mn.adventure.model.User;
import mn.adventure.service.ChallengeGenerator;
import mn.adventure.service.ProgressService;
import mn.adventure.ui.UiKit;
import mn.adventure.util.LevelSystem;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static mn.adventure.ui.UiKit.label;

/** dashboard.fxml-ийн controller: гол үзүүлэлт, өнөөдрийн ахиц, 7 хоногийн XP, сүүлийн амжилт. */
public class DashboardController implements PageController {
    @FXML private HBox statsRow;
    @FXML private VBox todayList;
    @FXML private Button todayButton;
    @FXML private Label levelNumber, levelTitle, levelHint;
    @FXML private ProgressBar levelBar;
    @FXML private BarChart<String, Number> weekChart;
    @FXML private VBox achievementList;

    private MainController main;

    @Override
    public void init(MainController main) {
        this.main = main;
        main.run(this::load);
    }

    private void load() throws Exception {
        User u = main.user();
        TaskDao taskDao = new TaskDao();
        List<Achievement> achievements = new ProgressService().loadWithProgress(u);
        long unlocked = achievements.stream().filter(Achievement::isUnlocked).count();
        int completed = taskDao.countCompleted(u.getId());

        // 1. Статистикийн 4 карт
        statsRow.getChildren().setAll(
                UiKit.statCard("▲", "#2dd4bf", "Түвшин", "Lv " + u.getLevel(), LevelSystem.title(u.getLevel())),
                UiKit.statCard("◆", "#fbbf24", "Нийт XP", String.format("%,d", u.getTotalXp()),
                        LevelSystem.xpToNext(u.getTotalXp()) + " XP дараагийн түвшин хүртэл"),
                UiKit.statCard("♨", "#f97316", "Streak", u.getEffectiveStreak(LocalDate.now()) + " өдөр",
                        "Шилдэг: " + u.getBestStreak() + " өдөр"),
                UiKit.statCard("★", "#a78bfa", "Амжилт", unlocked + " / " + achievements.size(),
                        completed + " сорил гүйцэтгэсэн"));

        // 2. Өнөөдрийн сорил
        List<DailyTask> today = new ChallengeGenerator().today(u);
        if (today.isEmpty()) {
            todayButton.setText("⚄  Сорил сугалах");
            todayButton.getStyleClass().setAll("button", "btn-primary");
            todayList.getChildren().setAll(label("Өнөөдрийн сорилоо хараахан сугалаагүй байна. Шоогоо хаяад эхлээрэй!", "muted"));
        } else {
            long done = today.stream().filter(DailyTask::isCompleted).count();
            ProgressBar bar = new ProgressBar((double) done / today.size());
            bar.setMaxWidth(Double.MAX_VALUE);
            todayList.getChildren().setAll(label(done + " / " + today.size() + " гүйцэтгэсэн", "muted"), bar);
            for (DailyTask t : today) {
                Label check = label(t.isCompleted() ? "✓" : "○", t.isCompleted() ? "success-text" : "muted");
                check.setStyle("-fx-font-size: 18px;");
                Label title = label(t.getChallenge().getTitle());
                if (t.isCompleted()) title.getStyleClass().add("muted");
                HBox r = new HBox(12, check, title, UiKit.spacer(),
                        UiKit.categoryChip(t.getChallenge().getCategory()),
                        label("+" + t.getChallenge().getXpReward() + " XP", "xp-chip"));
                r.setAlignment(Pos.CENTER_LEFT);
                todayList.getChildren().add(r);
            }
        }

        // 3. Түвшний карт
        levelNumber.setText(String.valueOf(u.getLevel()));
        levelTitle.setText(LevelSystem.title(u.getLevel()));
        levelBar.setProgress(LevelSystem.progress(u.getTotalXp()));
        levelHint.setText(LevelSystem.xpToNext(u.getTotalXp()) + " XP дутуу · Lv " + (u.getLevel() + 1));

        // 4. 7 хоногийн XP
        LocalDate from = LocalDate.now().minusDays(6);
        Map<LocalDate, Integer> xp = taskDao.xpByDay(u.getId(), from);
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = from.plusDays(i);
            s.getData().add(new XYChart.Data<>(d.getMonthValue() + "/" + d.getDayOfMonth(), xp.getOrDefault(d, 0)));
        }
        weekChart.getData().setAll(List.of(s));

        // 5. Сүүлийн амжилтууд
        List<Achievement> recent = achievements.stream().filter(Achievement::isUnlocked)
                .sorted(Comparator.comparing(Achievement::getUnlockedAt).reversed()).limit(4).toList();
        if (recent.isEmpty()) {
            achievementList.getChildren().setAll(label("Анхны сорилоо гүйцэтгээд эхний амжилтаа нээгээрэй!", "muted"));
        } else {
            achievementList.getChildren().clear();
            for (Achievement a : recent) {
                Label ic = label(a.getIcon(), "ach-icon");
                ic.setStyle("-fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40; -fx-font-size: 18px;");
                HBox r = new HBox(12, ic, new VBox(2, label(a.getTitle(), "h3"), label(a.getDescription(), "muted", "small")));
                r.setAlignment(Pos.CENTER_LEFT);
                achievementList.getChildren().add(r);
            }
        }
    }

    @FXML private void goToday() { main.navigate(MainController.PageKey.TODAY); }
    @FXML private void goAchievements() { main.navigate(MainController.PageKey.ACHIEVEMENTS); }
}
