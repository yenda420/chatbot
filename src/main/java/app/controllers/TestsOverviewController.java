package app.controllers;

import app.dao.*;
import app.enums.QuestionTypeEnum;
import app.enums.UserRoleEnum;
import app.enums.ViewEnum;
import app.models.*;
import app.services.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;

public class TestsOverviewController {
    private User currentUser;

    @FXML
    private ListView<String> tests;

    @FXML
    private Button logoutButton;

    @FXML
    private Button testsOverviewButton;

    @FXML
    private ImageView logoutIcon;

    @FXML
    private Text heading;

    @FXML
    private Button createFirstTestButton;

    @FXML
    private VBox formContainer;

    @FXML
    private HBox horizontalMenu;

    @FXML
    private void initialize() {
        logoutButton.setOnMouseEntered(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-maroon.png"))));
        logoutButton.setOnMouseExited(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-white.png"))));
    }

    public void initializeUserData() throws SQLException {
        if (currentUser != null) {
            showTestsListView();

            if (currentUser.getRole().equals(UserRoleEnum.ADMIN)) {
                DynamicService.setAdminNavigation(horizontalMenu, logoutButton, testsOverviewButton, getClass(), tests, currentUser, formContainer, ViewEnum.TESTS_OVERVIEW);
            }
        }
    }

    private void showTestsListView() throws SQLException {
        ArrayList<Test> testsFromDB = TestManager.getTests(currentUser);

        if (testsFromDB != null && !testsFromDB.isEmpty()) {
            ArrayList<String> testsArrayListString = new ArrayList<>();

            // Sort tests by ID
            testsFromDB.sort(Comparator.comparingInt(Test::getId));

            for (Test test : testsFromDB) {
                testsArrayListString.add(test.getName() + " | ID: " + test.getId());
            }

            ObservableList<String> testsList = FXCollections.observableArrayList(testsArrayListString);

            tests.setItems(testsList);
            tests.setVisible(true);
            createFirstTestButton.setVisible(false);
            initializeCustomCellFactory();
        } else {
            heading.setText("V databázi nejsou žádné testy z Vašich předmětů.");
            tests.setVisible(false);
            createFirstTestButton.setVisible(true);
        }
    }

    private void initializeCustomCellFactory() {
        DynamicService.setCellFactory(tests, this::handleDownload, this::handleDelete, "Stáhnout", currentUser.getRole().equals(UserRoleEnum.ADMIN));
    }

    private int getTestId(String item) {
        try {
            return Integer.parseInt(item.substring(item.indexOf("ID: ") + 4).trim());
        } catch (NumberFormatException e) {
            LogService.logError("Invalid test ID format: " + item.substring(item.indexOf("ID: ")));
            return -1;
        }
    }

    private void handleDelete(String item) {
        try {
            if (TestManager.deleteTestData(getTestId(item))) {
                showTestsListView();
            } else {
                LogService.logError("Failed to delete test " + item + " from database.");
            }
        } catch (Exception e) {
            LogService.logError("Error while deleting test: " + item + " from database.");
        }
    }

    private void handleDownload(String item) {
        try {
            int testId = getTestId(item);

            if (testId < 0) {
                LogService.logError("Unable to extract test ID from item.");
                return;
            }

            Test testForDownload = TestManager.getTestById(testId);

            if (testForDownload != null) {
                testForDownload.setId(testId);
            } else {
                LogService.logError("Test with ID " + testId + " not found in database.");
                return;
            }

            String testContent = generateTestContent(testForDownload);

            if (!FileService.writeTestToFile(testContent, testForDownload)) {
                LogService.logError("Failed to write test to file.");
                AlertService.showErrorAlert(
                        "Test se nepodařilo uložit z technických důvodů.",
                        "Chyba!",
                        "Aplikace selhala.");
            } else {
                LogService.logInfo("Test downloaded successfully.");
                AlertService.showSuccessAlert("Test byl uložen do Stažených souborů (Downloads).");
            }
        } catch (NumberFormatException e) {
            LogService.logError("Invalid test ID format.");
        } catch (SQLException e) {
            LogService.logError("SQL exception occurred while downloading test.");
            e.printStackTrace();
        }
    }

    private String generateTestContent(Test test) throws SQLException {
        StringBuilder contentBuilder = new StringBuilder();

        LogService.logInfo("Generating content for test: " + test.getName());

        contentBuilder.append("Název testu: ").append(test.getName()).append("\n");
        contentBuilder.append("Předmět: ").append(SubjectManager.getSubject(test.getPrompt().getTopics().get(0)).getName()).append("\n");
        contentBuilder.append("Témata: ");

        for (Topic topic : test.getPrompt().getTopics()) {
            contentBuilder.append(topic.getName()).append(", ");
        }

        if (!test.getPrompt().getTopics().isEmpty()) {
            contentBuilder.setLength(contentBuilder.length() - 2); // Remove last comma
        }

        contentBuilder.append("\n\n");
        contentBuilder.append("Obtížnost: ").append(test.getDifficulty().getName()).append("\n");
        contentBuilder.append("Časový limit: ").append(test.getTimeLimitInMinutes()).append(" minut\n\n");

        // Fetch questions
        ArrayList<Question> questions = QuestionManager.getQuestionsForTest(test.getId());

        int questionNumber = 1;

        for (Question question : questions) {
            contentBuilder.append(questionNumber).append(". ").append(question.getText()).append("\n");
            contentBuilder.append("Body: ").append(question.getPoints()).append("\n");

            ArrayList<Answer> answers = AnswerManager.getAnswersForQuestion(question.getId());
            if (test.getQuestionType().equals(QuestionTypeEnum.MULTIPLE_CHOICE)) {
                char option = 'a';
                for (Answer answer : answers) {
                    contentBuilder.append(option).append(") ").append(answer.getText()).append("\n");
                    option++;
                }
                contentBuilder.append("\n");
            } else if (test.getQuestionType().equals(QuestionTypeEnum.YES_NO)) {
                contentBuilder.append("Ano / Ne\n\n");
            } else if (test.getQuestionType().equals(QuestionTypeEnum.OPEN_ENDED)) {
                contentBuilder.append("\n");
            }

            questionNumber++;
        }

        // Correct Answers and Explanations
        contentBuilder.append("Správné odpovědi:\n");
        questionNumber = 1;

        for (Question question : questions) {
            ArrayList<Answer> answers = AnswerManager.getAnswersForQuestion(question.getId());
            for (Answer answer : answers) {
                if (answer.isCorrect()) {
                    contentBuilder.append(questionNumber).append(". ").append(answer.getText()).append("\n");

                    if (!test.getQuestionType().equals(QuestionTypeEnum.OPEN_ENDED)) {
                        contentBuilder.append("Vysvětlení: ").append(answer.getExplanation()).append("\n\n");
                    }
                }
            }
            questionNumber++;
        }

        return contentBuilder.toString();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void onGoToMain() {
        LoaderService.load(ViewEnum.MAIN, getClass(), tests, currentUser);
    }

    @FXML
    public void onGoToTopics() {
        LoaderService.load(ViewEnum.ADD_TOPIC, getClass(), tests, currentUser);
    }

    @FXML
    public void onGoToEditProfile() {
        LoaderService.load(ViewEnum.EDIT_PROFILE, getClass(), tests, currentUser);
    }

    @FXML
    public void onGoToTopicsOverview() {
        LoaderService.load(ViewEnum.TOPICS_OVERVIEW, getClass(), tests, currentUser);
    }

    @FXML
    public void onLogout() {
        LoaderService.load(ViewEnum.LOGIN, getClass(), tests, currentUser);
    }
}
