package app.controllers;

import app.dao.SubjectManager;
import app.enums.UserRoleEnum;
import app.enums.ViewEnum;
import app.models.Subject;
import app.models.User;
import app.services.DynamicService;
import app.services.LoaderService;
import app.services.LogService;
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

public class SubjectsOverviewController {
    private User currentUser;

    private ObservableList<String> subjectsList = null;

    @FXML
    private ListView<String> subjects;

    @FXML
    private Button logoutButton;

    @FXML
    private Button testsOverviewButton;

    @FXML
    private VBox formContainer;

    @FXML
    private ImageView logoutIcon;

    @FXML
    private HBox horizontalMenu;

    @FXML
    private Button createSubjectTopicButton;

    @FXML
    private Text heading;

    @FXML
    private void initialize() {
        logoutButton.setOnMouseEntered(event -> logoutIcon.setImage(
                new Image(getClass().getResourceAsStream("/images/logout-maroon.png"))));

        logoutButton.setOnMouseExited(event -> logoutIcon.setImage(
                new Image(getClass().getResourceAsStream("/images/logout-white.png"))));
    }

    public void initializeUserData() throws SQLException {
        if (currentUser != null) {
            showSubjectsListView();
            initializeCustomCellFactory();

            if (currentUser.getRole().equals(UserRoleEnum.ADMIN)) {
                DynamicService.setAdminNavigation(horizontalMenu, logoutButton, testsOverviewButton, getClass(), subjects, currentUser, formContainer, ViewEnum.SUBJECTS_OVERVIEW);
            }
        }
    }

    private void initializeCustomCellFactory() {
        DynamicService.setCellFactory(subjects, this::handleEdit, this::handleDelete, "Upravit", true);
    }

    private void handleEdit(String subjectName) {
        LoaderService.load(ViewEnum.EDIT_SUBJECT, getClass(), subjects, currentUser, new Subject(subjectName, SubjectManager.getAbbreviation(subjectName)));
    }

    private void handleDelete(String subjectName) {
        try {
            if (SubjectManager.delete(new Subject(subjectName, SubjectManager.getAbbreviation(subjectName)))) {
                showSubjectsListView();
            }
        } catch (SQLException e) {
            LogService.logInfo("Failed to delete subject " + subjectName + " from database.");
            e.printStackTrace();
        }
    }

    private void showSubjectsListView() throws SQLException {
        subjectsList = FXCollections.observableArrayList(SubjectManager.getSubjects(currentUser));

        if (!subjectsList.isEmpty()) {
            createSubjectTopicButton.setVisible(false);
            subjects.setVisible(true);
            subjects.setItems(subjectsList);
            heading.setText("Přehled předmětů");
        } else {
            createSubjectTopicButton.setVisible(true);
            subjects.setVisible(false);
            heading.setText("V databázi nejsou žádné předměty.");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void onGoToMain() {
        LoaderService.load(ViewEnum.MAIN, getClass(), subjects, currentUser);
    }

    @FXML
    public void onGoToEditProfile() {
        LoaderService.load(ViewEnum.EDIT_PROFILE, getClass(), subjects, currentUser);
    }

    @FXML
    public void onGoToTopicsOverview() {
        LoaderService.load(ViewEnum.TOPICS_OVERVIEW, getClass(), subjects, currentUser);
    }

    @FXML
    public void onGoToTopics() {
        LoaderService.load(ViewEnum.ADD_TOPIC, getClass(), subjects, currentUser);
    }

    @FXML
    public void onGoToTestsOverview() {
        LoaderService.load(ViewEnum.TESTS_OVERVIEW, getClass(), subjects, currentUser);
    }

    @FXML
    public void onGoToSubjects() {
        LoaderService.load(ViewEnum.ADD_SUBJECT, getClass(), subjects, currentUser);
    }

    @FXML
    public void onLogout() {
        LoaderService.load(ViewEnum.LOGIN, getClass(), subjects, currentUser);
    }
}
