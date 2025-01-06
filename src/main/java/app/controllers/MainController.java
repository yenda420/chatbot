package app.controllers;

import app.dao.*;
import app.enums.DifficultyEnum;
import app.enums.QuestionTypeEnum;
import app.enums.ViewEnum;
import app.models.Prompt;
import app.models.Test;
import app.models.Topic;
import app.models.User;
import app.services.AITestGeneratorService;

import app.services.LoaderService;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;

import static app.services.FileService.writeTestToFile;

public class MainController {

    private static final double CELL_HEIGHT_SMALLER = 36.7;
    private static final double CELL_HEIGHT_BIGGER = 39.9;
    private static final int MAX_CELLS_TO_SHOW = 11;

    private final ObservableList<String> subjectList =
            FXCollections.observableArrayList(SubjectManager.getSubjects());

    private File fileAttached;

    private ArrayList<Topic> chosenTopics = new ArrayList<>();

    private User currentUser;


    @FXML
    private TextField testName;

    @FXML
    private TextField questionCount;

    @FXML
    private TextField timeLimit;

    @FXML
    private ComboBox<String> subject;

    @FXML
    private ListView<String> topics;

    @FXML
    private ComboBox<String> difficulty;

    @FXML
    private ComboBox<String> questionType;

    @FXML
    private TextArea message;

    @FXML
    private Text hint;

    @FXML
    private Button createTestButton;

    @FXML
    private Text fileLabel;

    @FXML
    private Button fileButton;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Pane overlay;

    @FXML
    private Text heading;

    @FXML
    private Text explanation;

    @FXML
    public void handleCreateTest() {
        if (validateInputs()) {
            showLoader(true);

            // Define a Task to run in the background
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws SQLException, FileNotFoundException {
                    AITestGeneratorService ai = new AITestGeneratorService();
                    Prompt prompt = new Prompt(message.getText(), fileAttached, chosenTopics);
                    Test test = new Test(
                            testName.getText(),
                            Integer.parseInt(questionCount.getText()),
                            Integer.parseInt(timeLimit.getText()),
                            DifficultyEnum.fromString(difficulty.getValue()),
                            QuestionTypeEnum.fromString(questionType.getValue()),
                            prompt,
                            currentUser
                    );

                    String error = handleTestProcessing(test, prompt, ai);

                    if (error != null) {
                        showErrorAlert(testName, error);
                    }
                    return null;
                }
            };

            task.setOnSucceeded(event -> {
                showLoader(false);
                System.out.println("[INFO] - Task processed successfully.");
                showSuccessAlert();
                LoaderService.load(ViewEnum.MAIN, getClass(), testName, currentUser);
            });

            task.setOnFailed(event -> {
                showLoader(false);
                Throwable exception = task.getException();

                if (exception != null) {
                    System.err.println("[ERROR] - An exception occurred: " + exception.getMessage());
                }

                showErrorAlert("Test se nepodařilo vygenerovat z technických důvodů. Zkuste to, prosím později.");
                LoaderService.load(ViewEnum.MAIN, getClass(), testName, currentUser);
            });

            // Start the task in a new thread to keep UI responsive
            new Thread(task).start();
        }
    }

    @FXML
    private void showLoader(boolean show) {
        overlay.setVisible(show);
        progressIndicator.setVisible(show);
        shadeBackground(show);
    }

    private void shadeBackground(boolean shade) {
        testName.setDisable(shade);
        questionCount.setDisable(shade);
        timeLimit.setDisable(shade);
        difficulty.setDisable(shade);
        questionType.setDisable(shade);
        subject.setDisable(shade);
        topics.setDisable(shade);
        message.setDisable(shade);
        createTestButton.setDisable(shade);
        fileButton.setDisable(shade);
        hint.setDisable(shade);
        heading.setDisable(shade);
        explanation.setDisable(shade);
    }

    @FXML
    private void initialize() {
        createTestButton.setDefaultButton(true);
        testName.requestFocus();

        difficulty.getItems().addAll(QuestionManager.getQuestionDifficulties());
        questionType.getItems().addAll(QuestionManager.getQuestionTypes());
        topics.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        if (topics.getItems().isEmpty()) {
            topics.setPrefHeight(CELL_HEIGHT_SMALLER * 4);
        }

        topics.setCellFactory(param -> new ListCell<>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
                label.setStyle("-fx-font-weight: normal; -fx-text-fill: white;");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(label);
                    setPrefWidth(0);
                }
            }
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("[INFO] - Current user: " + user.getEmail());
    }

    public void initializeUserData() throws SQLException {
        if (currentUser != null) {
            ArrayList<String> subjects = SubjectManager.getSubjects(currentUser);
            ObservableList<String> subjectsObservable = FXCollections.observableArrayList(subjects);

            subject.setItems(subjectsObservable);
        } else {
            System.err.println("[ERROR] - User is not set.");
        }
    }

    @FXML
    public void handleChosenSubject() throws SQLException {
        if (subject.getValue() != null) {
            if (!subject.getValue().isEmpty() || !subject.getValue().isBlank()) {
                ObservableList<String> topicsObservable = FXCollections.observableArrayList();

                topicsObservable.addAll(TopicManager.getTopics(subject.getValue()));

                topics.setItems(topicsObservable);

                int numberOfCells = subjectList.size();

                if (numberOfCells > MAX_CELLS_TO_SHOW) {
                    topics.setPrefHeight(MAX_CELLS_TO_SHOW * CELL_HEIGHT_SMALLER);
                } else if (numberOfCells < 5) {
                    topics.setPrefHeight(numberOfCells * CELL_HEIGHT_BIGGER);
                } else {
                    topics.setPrefHeight(numberOfCells * CELL_HEIGHT_SMALLER);
                }

                hint.setText("Vyberte témata * (Ctrl + Klik):");
            }
        }
    }

    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileAttached = fileChooser.showOpenDialog(null);

        if (fileAttached != null) {
            fileLabel.setText("Přiložený soubor: " + fileAttached.getName());
            System.out.println("[INFO] - File selected: " + fileAttached.getAbsolutePath());
        } else {
            fileChooser.setTitle("Přiložený soubor nejde otevřít.");
            System.err.println("[ERROR] - File could not be opened.");
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

        if (!TestManager.save(test, currentUser, promptId)) {
            System.err.println("[ERROR] - Failed to save test " + test + "  into database.");
            return technicalError;
        }

        try {
            String testContent = ai.generateTest(test);

            // Check if the test was generated successfully
            if (testContent == null)
                return "AI z vašeho zadání nebylo schopné vygenerovat test. Zkuste to, prosím, znovu.";

            if (!TestManager.saveTestData(testContent, test, promptId))
                return technicalError;

            if (!writeTestToFile(testContent, test))
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

        chosenTopics.clear();

        for (String topic : topics.getSelectionModel().getSelectedItems()) {
            chosenTopics.add(new Topic(topic));
        }

        if (chosenTopics.isEmpty()) {
            showErrorAlert("Vyberte, prosím, alespoň jedno téma.");
            return false;
        }

        int questionCountInt;
        int timeLimitInt;

        try {
            questionCountInt = Integer.parseInt(questionCount.getText());

            if (questionCountInt <= 0) throw new NumberFormatException();

            final int MAX_QUESTIONS = Integer.parseInt(Dotenv.load().get("MAX_QUESTIONS"));

            if (questionCountInt > MAX_QUESTIONS) {
                showErrorAlert(questionCount, "Počet otázek je omezen na " + MAX_QUESTIONS + ".");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert(questionCount, "Počet otázek musí přirozené číslo.");
            return false;
        }

        try {
            timeLimitInt = Integer.parseInt(timeLimit.getText());

            if (timeLimitInt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showErrorAlert(timeLimit, "Časový limit musí být přirozené číslo.");
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

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Test byl uložen do Stažených souborů (Downloads).");
        alert.showAndWait();
    }
}