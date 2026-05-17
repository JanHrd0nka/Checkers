package cz.vse.java.checkers.client.Networking;


import cz.vse.java.checkers.client.Game.Controller;
import cz.vse.java.checkers.client.Game.WaitingRoomController;
import cz.vse.java.checkers.common.Message;
import cz.vse.java.checkers.common.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.vse.java.checkers.common.ServerMessage;


import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class SampleMessageHandler extends MessageHandler{
    Connection connection;

   private final ArrayList<Controller> controllers  = new ArrayList<>();

   private ResponseManager rm = ResponseManager.getInstance();

    // Stores pending responses mapped by their unique Correlation ID
    private final Map<String, CompletableFuture<Message>> pendingRequests = new ConcurrentHashMap<>();


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

        Message msg = new Message(message);
        logger.info("Server message: {}",msg);


        switch (ServerMessage.valueOf(msg.getToken())){
            case OK -> handleOK(msg);
            case PLAYERS_WAITING -> updateWaitingRoom(msg.getContent());
            case MATCH -> updateRequestingMatches(msg.getContent());
        }

    }

    public void registerController(Controller controller){
        controllers.add(controller);
    }

    private void handleOK(Message message){
        rm.dispatchResponse(message.getID(), message);
    }


    private void updateWaitingRoom(String message){
        logger.info("updating players");
        String[] players = message.split(", ");
        controllers.getFirst().updatePlayersWaiting(players);

    }

    private void updateRequestingMatches(String playerName){
        logger.info("updating requesting matches");
        controllers.getFirst().updateRequestingMatches(playerName);

    }




}
