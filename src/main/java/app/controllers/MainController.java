package app.controllers;

import app.dao.PromptManager;
import app.dao.QuestionManager;
import app.dao.TestManager;
import app.dao.TopicManager;

import app.enums.DifficultyEnum;
import app.enums.QuestionTypeEnum;
import app.models.Prompt;
import app.models.Test;
import app.models.Topic;
import app.services.AITestGeneratorService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import static app.services.FileService.writeTestToFile;

public class MainController {
    @FXML
    private TextField testName;

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
    private TextArea message;

    private File fileAttached;

    ArrayList<Topic> checkedTopics;

    @FXML
    private void initialize() {
        difficulty.getItems().addAll(QuestionManager.getQuestionDifficulties());
        questionType.getItems().addAll(QuestionManager.getQuestionTypes());

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
        fileAttached = fileChooser.showOpenDialog(null);
        if (fileAttached != null) {
            System.out.println("[INFO] - File selected: " + fileAttached.getAbsolutePath());
        }
    }

    @FXML
    public void handleCreateTest(ActionEvent actionEvent) throws SQLException {
        if (validateInputs()) {
            AITestGeneratorService aiTestGeneratorService = new AITestGeneratorService();

            Prompt prompt;
            Test test;

            boolean isFileAttached = this.fileAttached != null;
            boolean isMessage = !message.getText().isEmpty() && !message.getText().isBlank();
            int promptId;

            if (isFileAttached && isMessage) {
                prompt = new Prompt(message.getText(), fileAttached, checkedTopics, "test");
            } else if (isFileAttached) {
                prompt = new Prompt(fileAttached, checkedTopics, "test");
            } else if (isMessage) {
                prompt = new Prompt(message.getText(), checkedTopics, "test");
            } else {
                prompt = new Prompt(checkedTopics, "test");
            }

            test = new Test(
                    testName.getText(),
                    Integer.parseInt(questionCount.getText()),
                    Integer.parseInt(timeLimit.getText()),
                    DifficultyEnum.fromString(difficulty.getValue()),
                    QuestionTypeEnum.fromString(questionType.getValue()),
                    prompt
            );

            if ((promptId = PromptManager.insert(prompt)) != -1) {
                System.out.println("[INFO] - Prompt " + prompt + " inserted into database.");

                if (TestManager.insert(test, promptId)) {
                    try {
                        // Generate the test
                        String testContent = aiTestGeneratorService.generateTest(test);

                        // Check if the test was generated successfully
                        if (testContent != null) {
                            // Specify the output file path
                            String outputFilePath = testName.getText() + ".txt";

                            // Write the test to a file
                            writeTestToFile(testContent, outputFilePath);
                        } else {
                            String errorMessage = "AI z vašeho zadání nebylo schopné vygenerovat test. Zkuste to, prosím, znovu.";
                            showErrorAlert(testName, errorMessage);
                        }
                    } catch (SQLException e) {
                        System.err.println("[ERROR] - An error occurred while working with the database.");
                        e.printStackTrace();
                    }

                } else {
                    System.err.println("[ERROR] - Failed to insert test " + test + "  into database.");
                }
            } else {
                System.err.println("[ERROR] - Failed to insert prompt " + prompt + " into database.");
            }
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

        checkedTopics = new ArrayList<>();

        for (Node node : topicsPane.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                if (checkBox.isSelected()) {
                    checkedTopics.add(new Topic(checkBox.getText()));
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