package mn.adventure.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import mn.adventure.dao.CategoryDao;
import mn.adventure.dao.TaskDao;
import mn.adventure.model.Category;
import mn.adventure.model.Difficulty;
import mn.adventure.model.User;
import mn.adventure.ui.UiKit;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static mn.adventure.ui.UiKit.label;

/** stats.fxml-ийн controller: нийт үзүүлэлт, 14 хоногийн XP, ангиллын харьцаа, хүндрэлээр. */
public class StatsController implements PageController {
    @FXML private HBox summaryRow;
    @FXML private BarChart<String, Number> xpChart;
    @FXML private Label xpTotalLabel;
    @FXML private StackPane pieHolder;
    @FXML private PieChart categoryPie;
    @FXML private VBox legendBox;
    @FXML private HBox difficultyRow;

    @Override
    public void init(MainController main) {
        main.run(() -> load(main.user()));
    }

    private void load(User u) throws Exception {
        TaskDao dao = new TaskDao();
        int completed = dao.countCompleted(u.getId());
        int assigned = dao.countAssigned(u.getId());
        int rate = assigned == 0 ? 0 : Math.round(completed * 100f / assigned);
        summaryRow.getChildren().setAll(
                UiKit.statCard("✓", "#4ade80", "Гүйцэтгэсэн сорил", String.valueOf(completed), "нийт " + assigned + " оноогдсоноос"),
                UiKit.statCard("%", "#2dd4bf", "Гүйцэтгэлийн хувь", rate + "%", "солигдсоныг тооцохгүй"),
                UiKit.statCard("☀", "#fbbf24", "Идэвхтэй өдөр", String.valueOf(dao.countActiveDays(u.getId())), "ядаж 1 сорил хийсэн өдөр"),
                UiKit.statCard("♨", "#f97316", "Шилдэг streak", u.getBestStreak() + " өдөр", "дараалсан өдөр"));

        // 14 хоногийн XP
        LocalDate from = LocalDate.now().minusDays(13);
        Map<LocalDate, Integer> xp = dao.xpByDay(u.getId(), from);
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        int sum = 0;
        for (int i = 0; i < 14; i++) {
            LocalDate d = from.plusDays(i);
            int v = xp.getOrDefault(d, 0);
            sum += v;
            s.getData().add(new XYChart.Data<>(d.getMonthValue() + "/" + d.getDayOfMonth(), v));
        }
        xpChart.getData().setAll(List.of(s));
        xpTotalLabel.setText("Нийт " + sum + " XP");

        // Ангиллаар
        Map<String, Integer> byCat = dao.completedByCategory(u.getId());
        List<Category> cats = new CategoryDao().findAll();
        categoryPie.getData().clear();
        byCat.forEach((name, count) -> { if (count > 0) categoryPie.getData().add(new PieChart.Data(name + " (" + count + ")", count)); });
        for (PieChart.Data d : categoryPie.getData()) {
            cats.stream().filter(c -> d.getName().startsWith(c.getName())).findFirst()
                    .ifPresent(c -> d.getNode().setStyle("-fx-pie-color: " + c.getColor() + ";"));
        }
        if (categoryPie.getData().isEmpty()) pieHolder.getChildren().setAll(label("Гүйцэтгэсэн сорил хараахан алга.", "muted"));
        legendBox.getChildren().clear();
        for (Category c : cats) {
            Region dot = new Region();
            dot.setMinSize(12, 12);
            dot.setMaxSize(12, 12);
            dot.setStyle("-fx-background-color: " + c.getColor() + "; -fx-background-radius: 99;");
            HBox r = new HBox(8, dot, label(c.getName(), "small"), UiKit.spacer(),
                    label(String.valueOf(byCat.getOrDefault(c.getName(), 0)), "small", "muted"));
            r.setAlignment(Pos.CENTER_LEFT);
            legendBox.getChildren().add(r);
        }

        // Хүндрэлээр
        Map<String, Integer> byDiff = dao.completedByDifficulty(u.getId());
        int total = byDiff.values().stream().mapToInt(Integer::intValue).sum();
        difficultyRow.getChildren().clear();
        for (Difficulty d : Difficulty.values()) {
            int n = byDiff.getOrDefault(d.name(), 0);
            ProgressBar p = new ProgressBar(total == 0 ? 0 : (double) n / total);
            p.setMaxWidth(Double.MAX_VALUE);
            VBox col = new VBox(6, new HBox(8, UiKit.stars(d), label(d.getLabel(), "h3"), UiKit.spacer(),
                    label(n + " сорил", "muted")), p);
            HBox.setHgrow(col, Priority.ALWAYS);
            difficultyRow.getChildren().add(col);
        }
    }
}
