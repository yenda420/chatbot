package app.controllers;

import app.dao.TopicManager;
import app.enums.UserRoleEnum;
import app.enums.ViewEnum;
import app.models.Topic;
import app.models.User;
import app.services.DynamicService;
import app.services.LoaderService;
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

public class TopicsOverviewController {
    private User currentUser;

    private ObservableList<String> topicsList = null;

    @FXML
    private ListView<String> topics;

    @FXML
    private Button logoutButton;

    @FXML
    private Button testsOverviewButton;

    @FXML
    private Button createFirstTopicButton;

    @FXML
    private VBox formContainer;

    @FXML
    private ImageView logoutIcon;

    @FXML
    private HBox horizontalMenu;

    @FXML
    private Text heading;

    @FXML
    private void initialize() {
        logoutButton.setOnMouseEntered(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-maroon.png"))));
        logoutButton.setOnMouseExited(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-white.png"))));
    }

    public void initializeUserData() throws SQLException {
        if (currentUser != null) {
            showTopicsListView();
            initializeCustomCellFactory();

            if (currentUser.getRole().equals(UserRoleEnum.ADMIN)) {
                DynamicService.setAdminNavigation(horizontalMenu, logoutButton, testsOverviewButton, getClass(), topics, currentUser, formContainer, ViewEnum.TESTS_OVERVIEW);
            }
        }
    }

    private void initializeCustomCellFactory() {
        DynamicService.setCellFactory(topics, this::handleEdit, this::handleDelete, "Upravit", true);
    }

    private void handleEdit(String topicName) {
        LoaderService.load(ViewEnum.EDIT_TOPIC, getClass(), topics, currentUser, TopicManager.getTopic(topicName));
    }

    private void handleDelete(String topicName) {
        try {
            if (TopicManager.delete(new Topic(topicName))) {
                showTopicsListView();
            }
        } catch (SQLException e) {
            System.out.println("[INFO] - Failed to delete topic " + topicName + " from database.");
            e.printStackTrace();
        }
    }
    
    private void showTopicsListView() throws SQLException {
        topicsList = FXCollections.observableArrayList(TopicManager.getTopics(currentUser));
        
        if (!topicsList.isEmpty()) {
            topics.setItems(topicsList);
            topics.setVisible(true);
            createFirstTopicButton.setVisible(false);
            heading.setText("Přehled Vašich Tematických celků");
        } else {
            topics.setVisible(false);
            createFirstTopicButton.setVisible(true);
            heading.setText("V databázi nejsou žádné tematické celky z Vašich předmětů");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void onGoToMain() {
        LoaderService.load(ViewEnum.MAIN, getClass(), topics, currentUser);
    }

    @FXML
    public void onGoToTopics() {
        LoaderService.load(ViewEnum.ADD_TOPIC, getClass(), topics, currentUser);
    }

    @FXML
    public void onGoToEditProfile() {
        LoaderService.load(ViewEnum.EDIT_PROFILE, getClass(), topics, currentUser);
    }

    @FXML
    public void onGoToTestsOverview() {
        LoaderService.load(ViewEnum.TESTS_OVERVIEW, getClass(), topics, currentUser);
    }

    @FXML
    public void onLogout() {
        LoaderService.load(ViewEnum.LOGIN, getClass(), topics, currentUser);
    }
}
