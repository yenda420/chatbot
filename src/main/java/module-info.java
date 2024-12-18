module com.example.chatbot {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires com.google.gson;
    requires io.github.cdimascio.dotenv.java;
    requires com.google.common;

    opens app to javafx.fxml;
    exports app;
    exports app.controllers;
    opens app.controllers to javafx.fxml;
}