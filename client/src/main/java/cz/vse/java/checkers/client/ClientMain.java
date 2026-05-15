package cz.vse.java.checkers.client;

import cz.vse.java.checkers.client.Game.HelloApplication;
import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.MessageHandler;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import javafx.application.Application;

public class ClientMain {


    public static void main(String[] args) {


        Connection connection = new Connection();
        MessageHandler messageHandler = new SampleMessageHandler(connection);
        connection.addMessageHandler(messageHandler);
        connection.connect("localhost", 5000);

        Application.launch(HelloApplication.class, args);



    }

}
