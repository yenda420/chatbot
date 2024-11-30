package app.controllers;

import app.dao.TestManager;
import app.dao.TopicManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;

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
    private ComboBox<String> questionType;

    @FXML
    private FlowPane topicsPane;

    @FXML
    public TextArea message;

    @FXML
    private void initialize() {
        difficulty.getItems().addAll(TestManager.getQuestionDifficulties());
        questionType.getItems().addAll(TestManager.getQuestionTypes());

        for (String topic : TopicManager.getTopics()) {
            CheckBox checkBox = new CheckBox(topic);
            checkBox.setId("checkbox-" + topic);
            checkBox.getStyleClass().add("checkbox");
            topicsPane.getChildren().add(checkBox);
        }
    }

    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            System.out.println("File selected: " + file.getAbsolutePath());
        }
    }

    @FXML
    public void handleCreateTest(ActionEvent actionEvent) {
        if (validateInputs()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Success!");
            alert.showAndWait();
        }
    }

    private boolean validateInputs() {
        if (testName.getText().isEmpty() || testName.getText().isBlank()) {
            showErrorAlert(testName, "Vyplňte, prosím, pole pro název testu.");
            return false;
        }

        if (questionCount.getText().isEmpty() || questionCount.getText().isBlank()) {
            showErrorAlert(questionCount, "Vyplňte, prosím, pole pro počet otázek.");
            return false;
        }

        if (timeLimit.getText().isEmpty() || timeLimit.getText().isBlank()) {
            showErrorAlert(timeLimit, "Vyplňte, prosím, pole pro časový limit.");
            return false;
        }

        if (difficulty.getValue() == null) {
            showErrorAlert("Vyberte, prosím, obtížnost testu.");
            return false;
        }

        if (questionType.getValue() == null) {
            showErrorAlert("Vyberte, prosím, typ otázek.");
            return false;
        }

        ArrayList<String> checkedTopics = new ArrayList<>();

        for (Node node : topicsPane.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                if (checkBox.isSelected()) {
                    checkedTopics.add(checkBox.getText());
                }
            }
        }

        if (checkedTopics.isEmpty()) {
            showErrorAlert("Vyberte, prosím, alespoň jeden tématický celek.");
            return false;
        }

        int questionCountInt;
        int timeLimitInt;

        try {
            questionCountInt = Integer.parseInt(questionCount.getText());

            if (questionCountInt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showErrorAlert(questionCount, "Počet otázek musí být celé, kladné číslo.");
            return false;
        }

        try {
            timeLimitInt = Integer.parseInt(timeLimit.getText());

            if (timeLimitInt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showErrorAlert(timeLimit, "Časový limit musí být celé, kladné číslo.");
            return false;
        }

        return true;
    }

    private void showErrorAlert(TextField textField, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
        textField.requestFocus();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}