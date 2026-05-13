package cz.vse.java.checkers.client;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.MessageHandler;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getClassLoader().getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Checkers game");
        stage.setScene(scene);
        stage.show();
    }
}
