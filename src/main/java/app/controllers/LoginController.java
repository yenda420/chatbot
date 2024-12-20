package app.controllers;

import app.dao.UserManager;
import app.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
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
    private void initialize() {

    }

    @FXML
    private void handleLogin() throws SQLException {
        if (validateInputs()) {
            String passwordHash = Hashing.sha256()
                    .hashString(password.getText(), StandardCharsets.UTF_8)
                    .toString();

            try {
                User user = UserManager.getUser(email.getText());

                if (user == null) throw new SQLException("User not found.");

                // Load the Main View FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/fxml/main-view.fxml"));
                Parent root = loader.load();

                MainController mainController = loader.getController();
                mainController.setCurrentUser(user);

                // Get the current stage
                Stage stage = (Stage) email.getScene().getWindow();

                // Set the scene to the main view
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/app/style/style.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Generátor Testů");
            } catch (IOException e) {
                System.err.println("[ERROR] - Failed to load main-view.fxml.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to log in: " + e.getMessage());
            }
        }
    }

    private boolean validateInputs() throws SQLException {
        // Using early return
        if (email.getText().isBlank() || email.getText().isEmpty()) {
            showErrorAlert(email, "Zadejte, prosím, emailovou adresu.");
            return false;
        }

        if (password.getText().isBlank() || password.getText().isEmpty()) {
            showErrorAlert(password, "Vyplňte, prosím, pole pro heslo.");
            return false;
        }

        if (!instanceInDatabase("users", "email", email.getText())) {
            showErrorAlert(email, "Účet s tímto emailem neexistuje.");
            return false;
        }

        String usersPasswordHash = UserManager.getPasswordHash(email.getText());
        String enteredPasswordHash = Hashing.sha256()
                .hashString(password.getText(), StandardCharsets.UTF_8)
                .toString();

        if (usersPasswordHash == null || !usersPasswordHash.equals(enteredPasswordHash)) {
            showErrorAlert(password, "Chybné heslo.");
            return false;
        }

        return true;
    }

    private void showErrorAlert(Control control, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
        control.requestFocus();
    }
}