package app.controllers;

import app.dao.SubjectManager;
import app.dao.TopicManager;

import app.enums.ViewEnum;

import app.models.Subject;
import app.models.Topic;
import app.models.User;

import app.services.LoaderService;
import app.services.AlertService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
import java.util.ArrayList;

public class AddTopicController {
    private User currentUser;

    @FXML
    private Button addTopicButton;

    @FXML
    private ComboBox<String> subject;

    @FXML
    private TextField topicName;

    @FXML
    private Button logoutButton;

    @FXML
    private ImageView logoutIcon;

    @FXML
    private void initialize() {
        addTopicButton.setDefaultButton(true);
        topicName.requestFocus();

        logoutButton.setOnMouseEntered(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-maroon.png"))));
        logoutButton.setOnMouseExited(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-white.png"))));
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

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("[INFO] - Current user: " + user.getEmail());
    }

    @FXML
    public void onAddTopic() {
        if (validateInputs()) {
            try {
                String abbreviation = SubjectManager.getAbbreviation(subject.getValue());
                Topic topic = new Topic(topicName.getText(), new Subject(subject.getValue(), abbreviation));

                if (TopicManager.save(topic)) {
                    AlertService.showSuccessAlert("Tématický celek byl uloňen.");
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] - Failed to save topic.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onGoToMain() {
        LoaderService.load(ViewEnum.MAIN, getClass(), topicName, currentUser);
    }

    @FXML
    public void onGoToTestsOverview() {

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
    public void onLogout() {
        LoaderService.load(ViewEnum.LOGIN, getClass(), topicName, currentUser);
    }

    private boolean validateInputs() {
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
        return true;
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }
}
