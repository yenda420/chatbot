package app.services;

import app.controllers.AddTopicController;
import app.controllers.EditProfileController;
import app.controllers.MainController;
import app.enums.ViewEnum;

import app.models.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoaderService {
    public static void  load(ViewEnum view, Class<?> clazz, TextField someStageInput, User user) {
        load(view, clazz, (Stage) someStageInput.getScene().getWindow(), user);
    }

    public static void load(ViewEnum view, Class<?> clazz, Stage stage, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(clazz.getResource("/app/fxml/" + view.getName() + "-view.fxml"));
            Parent root = loader.load();

            if (view.equals(ViewEnum.MAIN)) {
                MainController mainController = loader.getController();
                mainController.setCurrentUser(user);
                mainController.initializeUserData();

            } else if (view.equals(ViewEnum.ADD_TOPIC)) {
                AddTopicController addTopicController = loader.getController();
                addTopicController.setCurrentUser(user);
                addTopicController.initializeUserData();

            } else if (view.equals(ViewEnum.EDIT_PROFILE)) {
                EditProfileController editProfileController = loader.getController();
                editProfileController.setCurrentUser(user);
                editProfileController.initializeUserData();
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

    private static String getTitle(ViewEnum view) {
        switch (view) {
            case REGISTER: return "Registrace";
            case LOGIN: return "Přihlášení";
            case MAIN: return "Generátor Testů";
            case ADD_TOPIC: return "Přidejte tématický celek";
            default: return "Neznámá stránka";
        }
    }
}