package app.controllers;

import app.dao.UserManager;
import app.enums.ViewEnum;

import app.services.LoaderService;
import app.services.AlertService;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import com.google.common.hash.Hashing;

import static app.services.DatabaseService.instanceInDatabase;

public class LoginController {
    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        loginButton.setDefaultButton(true);
        email.requestFocus();
    }

    @FXML
    private void handleRegisterLink() {
        LoaderService.load(ViewEnum.REGISTER, getClass(), email, null);
    }

    @FXML
    private void handleLogin() throws SQLException {
        if (validateInputs()) {
            LoaderService.load(ViewEnum.MAIN, getClass(), email, UserManager.getUser(email.getText()));
        }
    }

    private boolean validateInputs() throws SQLException {
        // Using early return
        if (email.getText().isBlank() || email.getText().isEmpty()) {
            AlertService.showErrorAlert(email, "Zadejte, prosím, emailovou adresu.");
            return false;
        }

        if (password.getText().isBlank() || password.getText().isEmpty()) {
            AlertService.showErrorAlert(password, "Vyplňte, prosím, pole pro heslo.");
            return false;
        }

        if (!instanceInDatabase("users", "email", email.getText())) {
            AlertService.showErrorAlert(email, "Účet s tímto emailem neexistuje.");
            return false;
        }

        String usersPasswordHash = UserManager.getPasswordHash(email.getText());
        String enteredPasswordHash = Hashing.sha256()
                .hashString(password.getText(), StandardCharsets.UTF_8)
                .toString();

        if (usersPasswordHash == null || !usersPasswordHash.equals(enteredPasswordHash)) {
            AlertService.showErrorAlert(password, "Chybné heslo.");
            return false;
        }

        return true;
    }
}