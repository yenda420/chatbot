package app.controllers;

import app.dao.TopicManager;
import app.enums.ViewEnum;
import app.models.Topic;
import app.models.User;
import app.services.LoaderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.sql.SQLException;

public class TopicsOverviewController {
    private User currentUser;

    private ObservableList<String> topicsList = null;

    @FXML
    private ListView<String> topics;

    @FXML
    private Button logoutButton;

    @FXML
    private ImageView logoutIcon;

    @FXML
    private void initialize() {
        logoutButton.setOnMouseEntered(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-maroon.png"))));
        logoutButton.setOnMouseExited(event -> logoutIcon.setImage(new Image(getClass().getResourceAsStream("/images/logout-white.png"))));
    }

    public void initializeUserData() throws SQLException {
        if (currentUser != null) {
            topicsList = FXCollections.observableArrayList(TopicManager.getTopics(currentUser));

            topics.setItems(topicsList);
            initializeCustomCellFactory();
        }
    }

    private void initializeCustomCellFactory() {
        topics.setCellFactory(lv -> new ListCell<>() {
            {
                // Instance initializer for event handling for each ListCell
                setOnMousePressed(event -> {
                    if (!isEmpty()) {
                        topics.getSelectionModel().clearSelection();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                // Called whenever a cell needs updating, such as scrolling or data changes
                super.updateItem(item, empty);

                // If the cell should display no content, set its graphic to null
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Button threeDotsButton = new Button("•••");
                    threeDotsButton.getStyleClass().add("three-dots-button");

                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem editItem = createMenuItem("Upravit");
                    MenuItem deleteItem = createMenuItem("Smazat");

                    editItem.getStyleClass().add("context-menu-item-top");
                    deleteItem.getStyleClass().add("context-menu-item-bottom");

                    editItem.setOnAction(event -> handleEdit(item));
                    deleteItem.setOnAction(event -> handleDelete(item));

                    contextMenu.getItems().addAll(editItem, deleteItem);

                    threeDotsButton.setOnAction(event -> contextMenu.show(threeDotsButton, Side.RIGHT, 0, 0));

                    Label label = new Label(item);
                    label.setTextFill(Color.WHITE);
                    label.setMaxWidth(400);
                    label.setWrapText(true);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    HBox hBox = new HBox(label, spacer, threeDotsButton);
                    hBox.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(hBox);
                }
            }
        });
    }

    private MenuItem createMenuItem(String text) {
        Label label = new Label(text);
        CustomMenuItem customItem = new CustomMenuItem(label);
        label.setStyle("-fx-text-fill: white; -fx-padding: 5;");
        customItem.setHideOnClick(false);
        return customItem;
    }

    private void handleEdit(String topicName) {
        LoaderService.load(ViewEnum.EDIT_TOPIC, getClass(), topics, currentUser, new Topic(topicName));
    }

    private void handleDelete(String topicName) {
        try {
            TopicManager.delete(new Topic(topicName));
            topicsList = FXCollections.observableArrayList(TopicManager.getTopics(currentUser));
            topics.setItems(topicsList);
        } catch (SQLException e) {
            System.out.println("[INFO] - Failed to delete topic " + topicName + " from database.");
            e.printStackTrace();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("[INFO] - Current user: " + user.getEmail());
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
