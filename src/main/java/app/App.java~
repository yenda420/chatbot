package app;

import app.enums.ViewEnum;
import app.services.DatabaseService;
import app.services.LoaderService;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        DatabaseService.initialize();

        LoaderService.load(ViewEnum.LOGIN, getClass(), stage, null);
    }

    @Override
    public void stop() throws Exception {
        DatabaseService.disconnect();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}