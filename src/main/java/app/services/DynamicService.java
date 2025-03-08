package app.services;

import app.enums.ViewEnum;
import app.models.User;

import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class DynamicService {
    private static final Button addSubjectButton = new Button("Přidat předmět");
    private static final Button subjectOverviewButton = new Button("Přehled předmětů");
    private static final Button addUserButton = new Button("Přidat uživatele");
    private static final Button userOverviewButton = new Button("Přehled uživatelů");

    public static void setAdminNavigation(
            HBox horizontalMenu,
            Button logoutButton,
            Button testsOverviewButton,
            Class<?> clazz,
            TextField someStageInput,
            User currentUser,
            VBox container,
            ViewEnum currentView
    ) {
        setButtons(horizontalMenu, logoutButton, testsOverviewButton, container, currentView);

        addUserButton.setOnAction(event -> LoaderService.load(ViewEnum.ADD_USER, clazz, someStageInput, currentUser));
        userOverviewButton.setOnAction(event -> LoaderService.load(ViewEnum.USERS_OVERVIEW, clazz, someStageInput, currentUser));
        addSubjectButton.setOnAction(event -> LoaderService.load(ViewEnum.ADD_SUBJECT, clazz, someStageInput, currentUser));
        subjectOverviewButton.setOnAction(event -> LoaderService.load(ViewEnum.SUBJECTS_OVERVIEW, clazz, someStageInput, currentUser));
    }

    public static void setAdminNavigation(
            HBox horizontalMenu,
            Button logoutButton,
            Button testsOverviewButton,
            Class<?> clazz,
            ListView<String> someStageInput,
            User currentUser,
            VBox container,
            ViewEnum currentView
    ) {
        setButtons(horizontalMenu, logoutButton, testsOverviewButton, container, currentView);

        addUserButton.setOnAction(event -> LoaderService.load(ViewEnum.ADD_USER, clazz, someStageInput, currentUser));
        userOverviewButton.setOnAction(event -> LoaderService.load(ViewEnum.USERS_OVERVIEW, clazz, someStageInput, currentUser));
        addSubjectButton.setOnAction(event -> LoaderService.load(ViewEnum.ADD_SUBJECT, clazz, someStageInput, currentUser));
        subjectOverviewButton.setOnAction(event -> LoaderService.load(ViewEnum.SUBJECTS_OVERVIEW, clazz, someStageInput, currentUser));
    }

    private static void setButtons(HBox horizontalMenu, Button logoutButton, Button testsOverviewButton, VBox container, ViewEnum currentView) {
        int logoutIndex = horizontalMenu.getChildren().indexOf(logoutButton);
        int generateTestsIndex = horizontalMenu.getChildren().indexOf(testsOverviewButton);

        addSubjectButton.getStyleClass().remove("menu-button-active");
        subjectOverviewButton.getStyleClass().remove("menu-button-active");
        addUserButton.getStyleClass().remove("menu-button-active");
        userOverviewButton.getStyleClass().remove("menu-button-active");

        addSubjectButton.getStyleClass().add("menu-button");
        subjectOverviewButton.getStyleClass().add("menu-button");
        addUserButton.getStyleClass().add("menu-button");
        userOverviewButton.getStyleClass().add("menu-button");

        switch (currentView) {
            case ADD_SUBJECT:
                addSubjectButton.getStyleClass().remove("menu-button");
                addSubjectButton.getStyleClass().add("menu-button-active");
                break;
            case SUBJECTS_OVERVIEW:
                subjectOverviewButton.getStyleClass().remove("menu-button");
                subjectOverviewButton.getStyleClass().add("menu-button-active");
                break;
            case ADD_USER:
                addUserButton.getStyleClass().remove("menu-button");
                addUserButton.getStyleClass().add("menu-button-active");
                break;
            case USERS_OVERVIEW:
                userOverviewButton.getStyleClass().remove("menu-button");
                userOverviewButton.getStyleClass().add("menu-button-active");
                break;
        }

        horizontalMenu.getChildren().add(generateTestsIndex + 1, addSubjectButton);
        horizontalMenu.getChildren().add(generateTestsIndex + 2, subjectOverviewButton);
        horizontalMenu.getChildren().add(logoutIndex + 1, addUserButton);
        horizontalMenu.getChildren().add(logoutIndex + 2, userOverviewButton);

        container.setPrefWidth(container.getPrefWidth() + 350);
    }

    public static void setCellFactory(
            ListView<String> listView,
            Consumer<String> customActionHandler,
            Consumer<String> deleteHandler,
            String customActionLabel,
            boolean isDeletable
    ) {
        listView.setCellFactory(lv -> new ListCell<>() {
            {
                // Instance initializer for event handling for each ListCell
                setOnMousePressed(event -> {
                    if (!isEmpty()) {
                        listView.getSelectionModel().clearSelection();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                // Called whenever a cell needs updating
                super.updateItem(item, empty);

                // If the cell should display no content, set its graphic to null
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem customActionItem = createMenuItem(customActionLabel);
                    Button threeDotsButton = new Button("•••");
                    threeDotsButton.getStyleClass().add("three-dots-button");

                    customActionItem.getStyleClass().add("context-menu-item-top");
                    customActionItem.setOnAction(event -> customActionHandler.accept(item));
                    contextMenu.getItems().add(customActionItem);

                    if (isDeletable) {
                        MenuItem deleteItem = createMenuItem("Smazat");
                        deleteItem.getStyleClass().add("context-menu-item-bottom");

                        deleteItem.setOnAction(event -> deleteHandler.accept(item));
                        contextMenu.getItems().add(deleteItem);
                    } else {
                        customActionItem.getStyleClass().remove("context-menu-item-top");
                        customActionItem.getStyleClass().add("context-menu-item");
                    }

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

    private static MenuItem createMenuItem(String text) {
        Label label = new Label(text);
        CustomMenuItem customItem = new CustomMenuItem(label);
        label.setStyle("-fx-text-fill: white; -fx-padding: 5;");
        customItem.setHideOnClick(false);
        return customItem;
    }
}
