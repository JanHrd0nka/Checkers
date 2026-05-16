package cz.vse.java.checkers.client.Networking;


import cz.vse.java.checkers.client.Game.WaitingRoomController;
import cz.vse.java.checkers.common.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;

public class SampleMessageHandler extends MessageHandler{
    Connection connection;

   private final ArrayList<WaitingRoomController> controllers  = new ArrayList<>();

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
        logger.info("Received message is: {}", message);

        ServerMessage msg = ServerMessage.valueOf(message.split(" ")[0]);
        logger.info("Server message: {}",msg);

        switch (msg){
            case PLAYERS_WAITING -> updateWaitingRoom(message);
            case MATCH -> updateRequestingMatches(message);
        }

    }

    public void registerController(WaitingRoomController controller){
        controllers.add(controller);
    }

    private void updateWaitingRoom(String message){
        logger.info("updating players");
        String[] players = message.substring(ServerMessage.PLAYERS_WAITING.name().length()+1).split(" ");
        controllers.getFirst().updatePlayersWaiting(players);

    }

    private void updateRequestingMatches(String message){
        logger.info("updating requesting matches");
        String playerName = message.split(" ")[1];
        controllers.getFirst().updateRequestingMatches(playerName);

    }


}
