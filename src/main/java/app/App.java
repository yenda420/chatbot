package app;

import app.dao.SubjectManager;
import app.dao.TopicManager;
import app.services.DatabaseService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/fxml/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("/app/style/style.css").toExternalForm());

        stage.setTitle("Generátor testů");
        stage.setScene(scene);
        stage.show();

        DatabaseService.initialize();

        /*
        String prompt = "Hello, how can I help you?";
        String response = AIService.askChatGPT(prompt);

        if (response != null) {
            System.out.println("ChatGPT Response: " + response);
        } else {
            System.out.println("Failed to get a response from ChatGPT.");
        }
        */

        DatabaseService.disconnect();
    }

    public static void main(String[] args) {
        launch();
    }
}