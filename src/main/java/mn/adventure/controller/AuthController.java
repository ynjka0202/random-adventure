package mn.adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import mn.adventure.App;
import mn.adventure.service.AuthService;
import mn.adventure.service.ValidationException;
import mn.adventure.ui.Theme;

/** auth.fxml-ийн controller: нэвтрэх, бүртгүүлэх, горим солих. */
public class AuthController {
    private final AuthService auth = new AuthService();

    @FXML private VBox loginPane;
    @FXML private VBox registerPane;
    @FXML private TextField loginField;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginError;
    @FXML private Button loginButton;
    @FXML private TextField regName;
    @FXML private TextField regUsername;
    @FXML private TextField regEmail;
    @FXML private PasswordField regPassword;
    @FXML private PasswordField regPassword2;
    @FXML private Label registerError;
    @FXML private Button registerButton;
    @FXML private Button themeButton;

    /** FXML ачаалагдсаны дараа JavaFX автоматаар дуудна. */
    @FXML
    private void initialize() {
        updateThemeText();
        showLogin();
    }

    @FXML
    private void handleLogin() {
        try {
            auth.login(loginField.getText(), loginPassword.getText());
            App.showMain();
        } catch (ValidationException ex) {
            loginError.setText(ex.getMessage());
        } catch (Exception ex) {
            loginError.setText("Алдаа: " + ex.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            auth.register(regName.getText(), regUsername.getText(), regEmail.getText(),
                    regPassword.getText(), regPassword2.getText());
            App.showMain();
        } catch (ValidationException ex) {
            registerError.setText(ex.getMessage());
        } catch (Exception ex) {
            registerError.setText("Алдаа: " + ex.getMessage());
        }
    }

    @FXML
    private void showLogin() {
        switchPane(true);
        loginField.requestFocus();
    }

    @FXML
    private void showRegister() {
        switchPane(false);
        regName.requestFocus();
    }

    /** Хоёр формыг солих: зөвхөн харагдаж буй формын товч Enter-т хариулна. */
    private void switchPane(boolean login) {
        loginPane.setVisible(login);
        loginPane.setManaged(login);
        registerPane.setVisible(!login);
        registerPane.setManaged(!login);
        loginButton.setDefaultButton(login);
        registerButton.setDefaultButton(!login);
        loginError.setText("");
        registerError.setText("");
    }

    @FXML
    private void toggleTheme() {
        Theme.apply(App.scene(), Theme.DARK.equals(Theme.current(App.scene())) ? Theme.LIGHT : Theme.DARK);
        updateThemeText();
    }

    private void updateThemeText() {
        if (App.scene() != null)
            themeButton.setText(Theme.DARK.equals(Theme.current(App.scene())) ? "☀  Light" : "☾  Dark");
    }
}
