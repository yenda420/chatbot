package app.controllers;

import app.dao.SubjectManager;
import app.dao.TopicManager;
import app.enums.ViewEnum;
import app.models.Subject;
import app.models.Topic;
import app.models.User;
import app.services.AlertService;
import app.services.LoaderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

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
    private void initialize() throws SQLException {
        if (currentUser != null) {
            ArrayList<String> subjects = SubjectManager.getSubjects(currentUser);
            ObservableList<String> subjectsObservable = FXCollections.observableArrayList(subjects);

            updateTopicButton.setDefaultButton(true);
            topicName.requestFocus();
            subject.setItems(subjectsObservable);
        } else {
            System.out.println("[WARRNING] - Current user is null. Expecting the LoaderService to update.");
        }
    }

    public void initializeData() throws SQLException {
        if (currentUser != null) {
            ArrayList<String> subjects = SubjectManager.getSubjects(currentUser);
            ObservableList<String> subjectsObservable = FXCollections.observableArrayList(subjects);

            subject.setItems(subjectsObservable);
        } else {
            System.err.println("[ERROR] - User is not set.");
        }

        if (topicToEdit != null) {
            topicName.setText(topicToEdit.getName());
            subject.getSelectionModel().select(SubjectManager.getSubject(topicToEdit).getName());
        } else {
            System.err.println("[ERROR] - Topic to edit is not set.");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("[INFO] - Current user: " + user.getEmail());
    }

    public void setTopicToEdit(Topic topicToEdit) {
        this.topicToEdit = topicToEdit;
        System.out.println("[INFO] - Topic to edit: " + topicToEdit.getName());
    }

    @FXML
    public void onUpdateTopic() {
        if (validateInputs()) {
            String abbreviation = SubjectManager.getAbbreviation(subject.getValue());
            Topic topic = new Topic(topicName.getText(), new Subject(subject.getValue(), abbreviation));

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
