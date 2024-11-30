package app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;

public class MainController {
    @FXML
    public TextField testName;

    @FXML
    private TextField questionCount;

    @FXML
    private TextField timeLimit;

    @FXML
    private ComboBox<String> difficulty;

    @FXML
    private CheckBox topicMath;

    @FXML
    private CheckBox topicScience;

    @FXML
    private CheckBox topicHistory;

    @FXML
    private ComboBox<String> questionType;

    public void initialize() {
        // Initialize logic, e.g., validate inputs.
    }

    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            System.out.println("File selected: " + file.getAbsolutePath());
        }
    }
}