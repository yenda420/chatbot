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

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
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

    private ArrayList<Topic> checkedTopics;

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
    public void handleCreateTest() throws SQLException, FileNotFoundException {
        if (validateInputs()) {
            AITestGeneratorService aiTestGeneratorService = new AITestGeneratorService();

            Prompt prompt = new Prompt(message.getText(), fileAttached, checkedTopics);

            Test test = new Test(
                    testName.getText(),
                    Integer.parseInt(questionCount.getText()),
                    Integer.parseInt(timeLimit.getText()),
                    DifficultyEnum.fromString(difficulty.getValue()),
                    QuestionTypeEnum.fromString(questionType.getValue()),
                    prompt
            );

            String error = handleTestProcessing(test, prompt, aiTestGeneratorService);

            if (error == null) {
                // Saving the test to the downloads folder + other actions will go here
                System.out.println("Success!");
            } else {
                showErrorAlert(testName, error);
            }
        }
    }

    private String handleTestProcessing(Test test, Prompt prompt, AITestGeneratorService ai) throws SQLException, FileNotFoundException {
        // Using early return

        String technicalError = "Test se nepodařilo vygenerovat z technických důvodů. Zkuste to, prosím později.";
        String fileError = "Test se nepodařilo zapsat do souboru z technických důvodů. Zkuste to, prosím později.";

        int promptId = PromptManager.save(prompt);

        if (promptId == -1) {
            System.err.println("[ERROR] - Failed to save prompt " + prompt + " into database.");
            return technicalError;
        }

        if (!TestManager.save(test, promptId)) {
            System.err.println("[ERROR] - Failed to save test " + test + "  into database.");
            return technicalError;
        }

        try {
            String testContent = ai.generateTest(test);

            // Check if the test was generated successfully
            if (testContent == null)
                return "AI z vašeho zadání nebylo schopné vygenerovat test. Zkuste to, prosím, znovu.";

            if (!TestManager.insertTestData(testContent, test, promptId))
                return technicalError;

            if (!writeTestToFile(testContent, testName.getText() + ".txt"))
                return fileError;

            return null;
        } catch (SQLException e) {
            System.err.println("[ERROR] - An error occurred while working with the database.");
            e.printStackTrace();
            return technicalError;
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