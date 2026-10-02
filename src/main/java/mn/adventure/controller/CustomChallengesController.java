package mn.adventure.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mn.adventure.App;
import mn.adventure.dao.CategoryDao;
import mn.adventure.dao.ChallengeDao;
import mn.adventure.model.Category;
import mn.adventure.model.Challenge;
import mn.adventure.model.Difficulty;
import mn.adventure.ui.Theme;
import mn.adventure.ui.UiKit;

import java.util.List;

import static mn.adventure.ui.UiKit.label;

/** custom.fxml-ийн controller: хэрэглэгч өөрийн сорилыг нэмж, устгана. */
public class CustomChallengesController implements PageController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Category> categoryBox;
    @FXML private ToggleGroup difficultyGroup;
    @FXML private ToggleButton easyButton, mediumButton, hardButton;
    @FXML private Label errorLabel;
    @FXML private VBox listCard;
    @FXML private Label listTitle;

    private final ChallengeDao dao = new ChallengeDao();
    private MainController main;

    @FXML
    private void initialize() {
        easyButton.setUserData(Difficulty.EASY);
        mediumButton.setUserData(Difficulty.MEDIUM);
        hardButton.setUserData(Difficulty.HARD);
        // Нэг хүндрэл заавал сонгогдсон байх
        difficultyGroup.selectedToggleProperty().addListener((o, oldT, newT) -> { if (newT == null) oldT.setSelected(true); });
    }

    @Override
    public void init(MainController main) {
        this.main = main;
        main.run(() -> {
            categoryBox.setItems(FXCollections.observableArrayList(new CategoryDao().findAll()));
            categoryBox.getSelectionModel().selectFirst();
            drawList(dao.findCustom(main.user().getId()));
        });
    }

    @FXML
    private void addChallenge() {
        errorLabel.setText("");
        String t = titleField.getText() == null ? "" : titleField.getText().trim();
        if (t.length() < 3) { errorLabel.setText("Сорилын нэр хамгийн багадаа 3 тэмдэгт байна."); return; }
        if (t.length() > 150) { errorLabel.setText("Сорилын нэр 150 тэмдэгтээс хэтрэхгүй."); return; }
        Difficulty d = (Difficulty) difficultyGroup.getSelectedToggle().getUserData();
        String desc = descriptionArea.getText() == null || descriptionArea.getText().isBlank() ? null : descriptionArea.getText().trim();
        Challenge ch = new Challenge(0, t, desc, categoryBox.getValue(), d, d.getBaseXp(), main.user().getId());
        if (main.run(() -> dao.insert(ch))) {
            main.toast("Сорил нэмэгдлээ. Одооноос санамсаргүй сонголтод орно!", false);
            main.reload();
        }
    }

    private void drawList(List<Challenge> mine) {
        listTitle.setText("Миний нэмсэн сорилууд (" + mine.size() + ")");
        listCard.getChildren().setAll(listTitle);
        if (mine.isEmpty()) listCard.getChildren().add(label("Одоогоор өөрийн сорил алга.", "muted"));
        for (Challenge c : mine) {
            Button del = new Button("Устгах");
            del.getStyleClass().add("btn-danger");
            del.setOnAction(e -> confirmDelete(c));
            VBox text = new VBox(2, label(c.getTitle(), "h3"),
                    label(c.getDescription() == null ? "" : c.getDescription(), "muted", "small"));
            HBox row = new HBox(12, UiKit.categoryChip(c.getCategory()), text, UiKit.spacer(),
                    UiKit.stars(c.getDifficulty()), label("+" + c.getXpReward() + " XP", "xp-chip"), del);
            row.setAlignment(Pos.CENTER_LEFT);
            listCard.getChildren().addAll(new Separator(), row);
        }
    }

    private void confirmDelete(Challenge c) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "\"" + c.getTitle() + "\" сорилыг устгах уу?",
                ButtonType.YES, ButtonType.NO);
        a.setTitle("Баталгаажуулах");
        a.setHeaderText(null);
        Theme.style(a.getDialogPane(), App.scene());
        a.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            if (main.run(() -> dao.deactivate(c.getId(), main.user().getId()))) {
                main.toast("Сорил устгагдлаа", false);
                main.reload();
            }
        });
    }
}
