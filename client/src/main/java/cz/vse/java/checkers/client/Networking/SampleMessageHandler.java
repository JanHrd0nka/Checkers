package cz.vse.java.checkers.client.Networking;

import cz.vse.java.checkers.client.Game.BoardController;
import cz.vse.java.checkers.common.ClientMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleMessageHandler extends MessageHandler{
    Connection connection;

    private final Logger logger = LoggerFactory.getLogger(BoardController.class);

    public SampleMessageHandler (Connection connection){
        this.connection = connection;
    }

    public void sendMove(){
        boolean result = connection.send(ClientMessage.MOVE, "2,3,3,4");
        if (result) {
            logger.info("Move message sent successfully");
        } else {
            logger.info("Move message failed");
        }
    }


    @Override
    public void onConnect() {
        System.out.println("Connected");
        connection.send(ClientMessage.LOGIN, "hrac1");
    }

    @Override
    public void onMessage(String message){
        System.out.println(message);
        //connection.removeMessageHandler(this);
    }
}
