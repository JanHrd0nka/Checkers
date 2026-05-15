package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.ClientMessage;

public class ConnectionController {

    private Connection connection = Connection.getInstance();
    private SampleMessageHandler messageHandler = new SampleMessageHandler(connection);

    public ConnectionController(){
        connection.connect("localhost", 5000);
        connection.addMessageHandler(messageHandler);
    }

    public boolean send(ClientMessage meesage, String string){
        return connection.send(meesage, string);
    }



}
