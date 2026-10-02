package mn.adventure.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import mn.adventure.dao.CategoryDao;
import mn.adventure.dao.TaskDao;
import mn.adventure.model.Category;
import mn.adventure.model.DailyTask;
import mn.adventure.model.TaskStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/** history.fxml-ийн controller: бүх сорилыг хүснэгтээр, хайлт, төлөв, ангиллаар шүүнэ. */
public class HistoryController implements PageController {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusBox;
    @FXML private ComboBox<Object> categoryBox;
    @FXML private Label countLabel;
    @FXML private TableView<DailyTask> table;
    @FXML private TableColumn<DailyTask, String> colDate, colTitle, colCategory, colDifficulty, colStatus, colXp;

    private FilteredList<DailyTask> filtered;

    /** Өнгөрсөн өдрийн хийгээгүй сорилыг "Хийгээгүй" гэж харуулна. */
    static String statusText(DailyTask t) {
        if (t.getStatus() == TaskStatus.PENDING && t.getAssignedDate().isBefore(LocalDate.now())) return "Хийгээгүй";
        return t.getStatus().getLabel();
    }

    @FXML
    private void initialize() {
        // Баганын утгыг хаанаас авахыг заана
        bind(colDate, t -> t.getAssignedDate().format(FMT));
        bind(colTitle, t -> t.getChallenge().getTitle() + (t.isBonus() ? "  ★ бонус" : ""));
        bind(colCategory, t -> t.getChallenge().getCategory().toString());
        bind(colDifficulty, t -> t.getChallenge().getDifficulty().starText());
        bind(colStatus, HistoryController::statusText);
        bind(colXp, t -> t.getXpEarned() > 0 ? "+" + t.getXpEarned() : "—");
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty ? null : s);
                setStyle(empty ? "" : switch (s) {
                    case "Гүйцэтгэсэн" -> "-fx-text-fill: -c-success; -fx-font-weight: bold;";
                    case "Хийгээгүй" -> "-fx-text-fill: -c-danger;";
                    case "Солигдсон" -> "-fx-text-fill: -c-muted;";
                    default -> "-fx-text-fill: -c-gold;";
                });
            }
        });
        statusBox.setItems(FXCollections.observableArrayList("Бүх төлөв", "Гүйцэтгэсэн", "Хүлээгдэж буй", "Хийгээгүй", "Солигдсон"));
        statusBox.getSelectionModel().selectFirst();
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        statusBox.setOnAction(e -> applyFilter());
        categoryBox.setOnAction(e -> applyFilter());
    }

    @Override
    public void init(MainController main) {
        main.run(() -> {
            filtered = new FilteredList<>(FXCollections.observableArrayList(new TaskDao().history(main.user().getId())));
            table.setItems(filtered);
            categoryBox.getItems().setAll("Бүх ангилал");
            categoryBox.getItems().addAll(new CategoryDao().findAll());
            categoryBox.getSelectionModel().selectFirst();
            applyFilter();
        });
    }

    private void applyFilter() {
        if (filtered == null) return;
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String st = statusBox.getValue();
        Object cat = categoryBox.getValue();
        filtered.setPredicate(t -> {
            if (!q.isEmpty() && !t.getChallenge().getTitle().toLowerCase().contains(q)) return false;
            if (st != null && !"Бүх төлөв".equals(st) && !statusText(t).equals(st)) return false;
            return !(cat instanceof Category c) || t.getChallenge().getCategory().getId() == c.getId();
        });
        countLabel.setText(filtered.size() + " бичлэг");
    }

    private static void bind(TableColumn<DailyTask, String> col, Function<DailyTask, String> f) {
        col.setCellValueFactory(d -> new SimpleStringProperty(f.apply(d.getValue())));
    }
}
