package cz.vse.java.checkers.client;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.MessageHandler;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        Connection connection = new Connection();
        MessageHandler handler = new SampleMessageHandler(connection);
        connection.addMessageHandler(handler);
        connection.connect("localhost", 5000);

        Application.launch(HelloApplication.class, args);
    }
}
