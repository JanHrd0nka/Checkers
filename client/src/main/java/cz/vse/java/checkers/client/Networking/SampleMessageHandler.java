package cz.vse.java.checkers.client.Networking;

import cz.vse.java.checkers.common.ClientMessage;

public class SampleMessageHandler extends MessageHandler{
    Connection connection;
    public SampleMessageHandler (Connection connection){
        this.connection = connection;
    }

    @Override
    public void onConnect() {
        System.out.println("Connected");
        connection.send(ClientMessage.LOGIN, "hrac1");
    }

    @Override
    public void onMessage(String message){
        System.out.println(message);
    }
}
