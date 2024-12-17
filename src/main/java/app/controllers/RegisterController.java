package app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.regex.Pattern;

public class RegisterController {
    @FXML
    private TextField username;

    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private void handleRegister() {
        if (validateInputs()) {
            System.out.println("Registration successful!");
            // Handle successful registration
        }
    }

    private boolean validateInputs() {
        if (username.getText().isBlank()) {
            showErrorAlert(username, "Vyplňte, prosím, pole pro uživatelské jméno.");
            return false;
        }

        if (!isValidEmail(email.getText())) {
            showErrorAlert(email, "Zadejte, prosím, platnou emailovou adresu.");
            return false;
        }

        if (password.getText().isBlank()) {
            showErrorAlert(password, "Vyplňte, prosím, pole pro heslo.");
            return false;
        }

        if (!password.getText().equals(confirmPassword.getText())) {
            showErrorAlert(confirmPassword, "Hesla se neshodují.");
            return false;
        }

        return true;
    }

    private void showErrorAlert(Control control, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
        control.requestFocus();
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.compile(emailPattern).matcher(email).matches();
    }
}