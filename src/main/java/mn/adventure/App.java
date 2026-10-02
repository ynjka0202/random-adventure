package mn.adventure;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import mn.adventure.db.Database;
import javafx.fxml.FXMLLoader;
import mn.adventure.controller.MainController;
import mn.adventure.ui.Theme;
import mn.adventure.util.Session;

/** JavaFX програмын үндсэн класс: цонх үүсгэж, дэлгэц хооронд шилжүүлнэ. */
public class App extends Application {
    private static Stage stage;
    private static Scene scene;
    private static MainController mainController;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Random Adventure — Санамсаргүй адал явдал");
        stage.setMinWidth(1040);
        stage.setMinHeight(700);

        scene = new Scene(new javafx.scene.layout.Pane(), 1240, 800);
        Theme.apply(scene, Theme.DARK);
        stage.setScene(scene);

        if (!checkDatabase()) return;
        showAuth();
        stage.show();
    }

    /** Өгөгдлийн сантай холбогдож чадах эсэхийг шалгаад, чадахгүй бол заавар харуулна. */
    private boolean checkDatabase() {
        try {
            Database.get().connection();
            return true;
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Өгөгдлийн сантай холбогдож чадсангүй");
            a.setHeaderText("MySQL сервер ажиллаж байгаа эсэхийг шалгана уу");
            TextArea ta = new TextArea("""
                    1. MySQL Server асаалттай эсэхийг шалгана (Services → MySQL80).
                    2. database/schema.sql-ийг MySQL Workbench дээр ажиллуулсан эсэх.
                    3. src/main/resources/db.properties доторх нууц үг зөв эсэх.

                    URL: %s
                    Алдаа: %s""".formatted(Database.get().url(), e.getMessage()));
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefRowCount(8);
            a.getDialogPane().setContent(ta);
            a.showAndWait();
            return false;
        }
    }

    /** Нэвтрэх дэлгэц (auth.fxml). */
    public static void showAuth() {
        Session.logout();
        Theme.apply(scene, Theme.current(scene));
        scene.setRoot(load("auth.fxml").getRoot());
    }

    /** Үндсэн цонх (main.fxml). Хэрэглэгчийн сонгосон горимоор нээнэ. */
    public static void showMain() {
        String theme = Session.user().getTheme();
        Theme.apply(scene, theme == null ? Theme.DARK : theme);
        FXMLLoader loader = load("main.fxml");
        mainController = loader.getController();
        scene.setRoot(loader.getRoot());
    }

    /** resources/fxml доторх FXML файлыг ачаална. */
    private static FXMLLoader load(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml));
            loader.load();
            return loader;
        } catch (Exception e) {
            throw new IllegalStateException(fxml + " ачаалж чадсангүй: " + e.getMessage(), e);
        }
    }

    public static MainController mainController() { return mainController; }

    public static Scene scene() { return scene; }

    @Override
    public void stop() {
        Database.get().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
