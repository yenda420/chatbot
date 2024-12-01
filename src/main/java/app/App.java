package app;

import app.services.DatabaseService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseService.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/fxml/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("/app/style/style.css").toExternalForm());

        stage.setTitle("Generátor testů");
        stage.setScene(scene);
        // stage.setFullScreen(true);
        stage.show();

        /*
        String prompt = "Hello, how can I help you?";
        String response = AIService.askChatGPT(prompt);

        if (response != null) {
            System.out.println("ChatGPT Response: " + response);
        } else {
            System.out.println("Failed to get a response from ChatGPT.");
        }
        */
    }

    @Override
    public void stop() throws Exception {
        // This method is called when the application is about to close
        DatabaseService.disconnect();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}