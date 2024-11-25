module com.example.chatbot {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.chatbot to javafx.fxml;
    exports com.example.chatbot;
    exports com.example.chatbot.controllers;
    opens com.example.chatbot.controllers to javafx.fxml;
}