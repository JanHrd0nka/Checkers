package cz.vse.java.checkers.client.Networking;

import cz.vse.java.checkers.client.Game.WaitingRoomController;
import cz.vse.java.checkers.common.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleMessageHandler extends MessageHandler{
    Connection connection;

    private final Logger logger = LoggerFactory.getLogger(SampleMessageHandler.class);


    public SampleMessageHandler(Connection connection){
        this.connection = connection;
    }



    @Override
    public void onConnect() {
        System.out.println("Connected");
    }

    @Override
    public void onMessage(String message){
        logger.info("Received message: {}", message);

        switch (ServerMessage.valueOf(message.split(" ")[0])){
            case PLAYERS_WAITING -> updateWaitingRoom(message);
        }

    }

    private void updateWaitingRoom(String message){
        logger.info("updating players");
        WaitingRoomController wrc = new WaitingRoomController();
        wrc.updatePlayersWaiting(message.substring(ServerMessage.PLAYERS_WAITING.name().length()));
    }

}
