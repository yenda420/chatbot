package app.controllers;

import app.dao.SubjectManager;
import app.dao.TopicManager;
import app.enums.UserRoleEnum;
import app.enums.ViewEnum;
import app.models.Subject;
import app.models.Topic;
import app.models.User;
import app.services.AlertService;
import app.services.DatabaseService;
import app.services.LoaderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;

public class EditTopicController {
    private User currentUser;

    private Topic topicToEdit;

    @FXML
    private Button updateTopicButton;

    @FXML
    private ComboBox<String> subject;

    @FXML
    private TextField topicName;

    @FXML
    private Button logoutButton;

    @FXML
    private ImageView logoutIcon;

    @FXML
    private HBox horizontalMenu;

    @FXML
    private VBox content;

    @FXML
    private CheckBox isPrivateCheckbox;

    @FXML
    private void initialize() {
        updateTopicButton.setDefaultButton(true);
        topicName.requestFocus();

        logoutButton.setOnMouseEntered(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-maroon.png"))));
        logoutButton.setOnMouseExited(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-white.png"))));
    }

    public void initializeData() throws SQLException {
        if (currentUser != null) {
            ArrayList<String> subjects = SubjectManager.getSubjects(currentUser);
            ObservableList<String> subjectsObservable = FXCollections.observableArrayList(subjects);

            subject.setItems(subjectsObservable);

            if (currentUser.getRole().equals(UserRoleEnum.ADMIN)) {
                int logoutIndex = horizontalMenu.getChildren().indexOf(logoutButton);
                Button addUserButton = new Button("Přidat uživatele");
                Button userOverviewButton = new Button("Přehled uživatelů");

                addUserButton.setOnAction(event -> LoaderService.load(ViewEnum.ADD_USER, getClass(), topicName, currentUser));
                userOverviewButton.setOnAction(event -> LoaderService.load(ViewEnum.USERS_OVERVIEW, getClass(), topicName, currentUser));

                addUserButton.getStyleClass().add("menu-button");
                userOverviewButton.getStyleClass().add("menu-button");

                horizontalMenu.getChildren().add(logoutIndex, addUserButton);
                horizontalMenu.getChildren().add(logoutIndex + 1, userOverviewButton);

                content.setPrefWidth(content.getPrefWidth() + 400);
            }
        } else {
            System.err.println("[ERROR] - User is not set.");
        }

        if (topicToEdit != null) {
            topicName.setText(topicToEdit.getName());
            subject.getSelectionModel().select(SubjectManager.getSubject(topicToEdit).getName());
            isPrivateCheckbox.setSelected(topicToEdit.isPrivate());
        } else {
            System.err.println("[ERROR] - Topic to edit is not set.");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setTopicToEdit(Topic topicToEdit) {
        this.topicToEdit = topicToEdit;
    }

    @FXML
    public void onUpdateTopic() throws SQLException {
        if (validateInputs()) {
            String abbreviation = SubjectManager.getAbbreviation(subject.getValue());
            Topic topic = new Topic(topicName.getText(), new Subject(subject.getValue(), abbreviation), isPrivateCheckbox.isSelected(), currentUser);

            if (TopicManager.update(topicToEdit, topic)) {
                AlertService.showSuccessAlert("Tématický celek byl upraven.");
                LoaderService.load(ViewEnum.TOPICS_OVERVIEW, getClass(), topicName, currentUser);
            }
        }
    }

    @FXML
    public void onGoToMain() {
        LoaderService.load(ViewEnum.MAIN, getClass(), topicName, currentUser);
    }

    @FXML
    public void onGoToEditProfile() {
        LoaderService.load(ViewEnum.EDIT_PROFILE, getClass(), topicName, currentUser);
    }

    @FXML
    public void onGoToTopicsOverview() {
        LoaderService.load(ViewEnum.TOPICS_OVERVIEW, getClass(), topicName, currentUser);
    }

    @FXML
    public void onGoToTopics() {
        LoaderService.load(ViewEnum.ADD_TOPIC, getClass(), topicName, currentUser);
    }

    @FXML
    public void onGoToTestsOverview() {
        LoaderService.load(ViewEnum.TESTS_OVERVIEW, getClass(), topicName, currentUser);
    }

    @FXML
    public void onLogout() {
        LoaderService.load(ViewEnum.LOGIN, getClass(), topicName, currentUser);
    }

    private boolean validateInputs() throws SQLException {
        String text = topicName.getText();

        if (text.isEmpty() || text.isBlank()) {
            AlertService.showErrorAlert(topicName, "Vyplňte, prosím, pole pro název témata.");
            return false;
        }

        if (isNumeric(text)) {
            AlertService.showErrorAlert(topicName, "Název témata by nemělo být pouze číslo.");
            return false;
        }

        if (subject.getValue() == null) {
            AlertService.showErrorAlert("Vyberte, prosím, obtížnost testu.");
            return false;
        }

        if (!topicToEdit.getName().equals(topicName.getText())) {
            if (DatabaseService.instanceInDatabase("topics", "name", topicName.getText())) {
                AlertService.showErrorAlert(topicName, "Tématický celek s tímto názvem již existuje.");
                return false;
            }
        }

        return true;
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }
}
