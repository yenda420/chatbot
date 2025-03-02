package app.services;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.util.Optional;

public class AlertService {
    public static void showErrorAlert(TextField textField, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle("Chyba!");
        alert.setHeaderText("Validace selhala.");
        alert.showAndWait();
        textField.requestFocus();
    }

    public static void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle("Chyba!");
        alert.setHeaderText("Validace selhala.");
        alert.showAndWait();
    }

    public static void showErrorAlert(String message, String title, String header) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    public static void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setTitle("Úspěch!");
        alert.setHeaderText("Následující akce proběhla úspěšně.");
        alert.showAndWait();
    }

    public static boolean showConfirmationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType confirm = new ButtonType("Ano", ButtonType.YES.getButtonData());
        ButtonType cancel = new ButtonType("Ne", ButtonType.NO.getButtonData());

        alert.getButtonTypes().setAll(confirm, cancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirm;
    }
}
