package app.services;

import app.controllers.*;
import app.enums.ViewEnum;

import app.models.Topic;
import app.models.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoaderService {
    public static void load(ViewEnum view, Class<?> clazz, TextField someStageInput, User currentUser) {
        load(view, clazz, (Stage) someStageInput.getScene().getWindow(), currentUser);
    }

    public static void load(ViewEnum view, Class<?> clazz, ListView someStageInput, User currentUser, Topic topicToEdit) {
        load(view, clazz, (Stage) someStageInput.getScene().getWindow(), currentUser, topicToEdit);
    }

    public static void load(ViewEnum view, Class<?> clazz, ListView someStageInput, User currentUser) {
        load(view, clazz, (Stage) someStageInput.getScene().getWindow(), currentUser);
    }

    public static void load(ViewEnum view, Class<?> clazz, ListView someStageInput, User currentUser, User userToEdit) {
        load(view, clazz, (Stage) someStageInput.getScene().getWindow(), currentUser, userToEdit);
    }

    public static void load(ViewEnum view, Class<?> clazz, Stage stage, User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(clazz.getResource("/app/fxml/" + view.getName() + "-view.fxml"));
            Parent root = loader.load();

            switch (view) {
                case MAIN:
                    MainController mainController = loader.getController();
                    mainController.setCurrentUser(currentUser);
                    mainController.initializeUserData();
                    break;

                case ADD_TOPIC:
                    AddTopicController addTopicController = loader.getController();
                    addTopicController.setCurrentUser(currentUser);
                    addTopicController.initializeUserData();
                    break;

                case EDIT_PROFILE:
                    EditProfileController editProfileController = loader.getController();
                    editProfileController.setCurrentUser(currentUser);
                    editProfileController.initializeUserData();
                    break;

                case TOPICS_OVERVIEW:
                    TopicsOverviewController topicsOverviewController = loader.getController();
                    topicsOverviewController.setCurrentUser(currentUser);
                    topicsOverviewController.initializeUserData();
                    break;

                case EDIT_TOPIC:
                    EditProfileController editTopicController = loader.getController();
                    editTopicController.setCurrentUser(currentUser);
                    break;

                case TESTS_OVERVIEW:
                    TestsOverviewController testsOverviewController = loader.getController();
                    testsOverviewController.setCurrentUser(currentUser);
                    testsOverviewController.initializeUserData();
                    break;

                case ADD_USER:
                    AddUserController addUserController = loader.getController();
                    addUserController.setCurrentUser(currentUser);
                    addUserController.initializeUserData();
                    break;

                case USERS_OVERVIEW:
                    UsersOverviewController usersOverviewController = loader.getController();
                    usersOverviewController.setCurrentUser(currentUser);
                    usersOverviewController.initializeUserData();
                    break;

                default:
                    System.out.println("[WARNING] - No specified actions for view: " + view.getName());
                    break;
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(clazz.getResource("/app/style/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle(getTitle(view));
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("[ERROR] - Failed to load " + view.getName() + "-view.fxml.");
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load(ViewEnum view, Class<?> clazz, Stage stage, User currentUser, Topic topicToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(clazz.getResource("/app/fxml/" + view.getName() + "-view.fxml"));
            Parent root = loader.load();

            if (view.equals(ViewEnum.EDIT_TOPIC)) {
                EditTopicController editTopicController = loader.getController();
                editTopicController.setCurrentUser(currentUser);
                editTopicController.setTopicToEdit(topicToEdit);
                editTopicController.initializeData();
            } else {
                System.out.println("[WARNING] - No specified actions for view: " + view.getName());
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(clazz.getResource("/app/style/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle(getTitle(view));
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("[ERROR] - Failed to load " + view.getName() + "-view.fxml.");
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load(ViewEnum view, Class<?> clazz, Stage stage, User currentUser, User userToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(clazz.getResource("/app/fxml/" + view.getName() + "-view.fxml"));
            Parent root = loader.load();

            if (view.equals(ViewEnum.EDIT_PROFILE)) {
                EditProfileController editProfileController = loader.getController();
                editProfileController.setCurrentUser(currentUser);
                editProfileController.setUserToEdit(userToEdit);
                editProfileController.initializeUserData();
            } else {
                System.out.println("[WARNING] - No specified actions for view: " + view.getName());
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(clazz.getResource("/app/style/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Změna údajů uživatele");
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("[ERROR] - Failed to load " + view.getName() + "-view.fxml.");
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getTitle(ViewEnum view) {
        return switch (view) {
            case REGISTER -> "Registrace";
            case LOGIN -> "Přihlášení";
            case MAIN -> "Generátor testů";
            case ADD_TOPIC -> "Přidejte tématický celek";
            case ADD_USER -> "Přidejte uživatele";
            case TOPICS_OVERVIEW -> "Přehled tématických celků";
            case TESTS_OVERVIEW -> "Přehled testů z Vašich předmětů";
            case USERS_OVERVIEW -> "Přehled uživatelů";
            case EDIT_TOPIC -> "Změna tématického celku";
            case EDIT_PROFILE -> "Váš účet";
            default -> "Neznámá stránka";
        };
    }
}