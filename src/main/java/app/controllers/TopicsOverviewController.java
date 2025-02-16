package app.controllers;

import app.dao.TopicManager;
import app.enums.ViewEnum;
import app.models.User;
import app.services.LoaderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.sql.SQLException;

public class TopicsOverviewController {
    private static final double CELL_HEIGHT_SMALLER = 36.7;
    private static final double CELL_HEIGHT_BIGGER = 39.9;
    private static final int MAX_CELLS_TO_SHOW = 11;

    private User currentUser;

    @FXML
    private ListView<String> topics;

    public void initializeUserData() throws SQLException {
        if (currentUser != null) {
            ObservableList<String> topicsList = FXCollections.observableArrayList(TopicManager.getTopics(currentUser));

            int numberOfCells = topicsList.size();
            adjustListViewHeight(numberOfCells);
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

    private void handleEdit(String item) {
        // TODO
    }

    private void handleDelete(String item) {
        // TODO
    }

    private void adjustListViewHeight(int numberOfCells) {
        if (numberOfCells > MAX_CELLS_TO_SHOW) {
            topics.setPrefHeight(MAX_CELLS_TO_SHOW * CELL_HEIGHT_SMALLER);
        } else if (numberOfCells < 5) {
            topics.setPrefHeight(numberOfCells * CELL_HEIGHT_BIGGER);
        } else {
            topics.setPrefHeight(numberOfCells * CELL_HEIGHT_SMALLER);
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
}
