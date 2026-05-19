package cz.vse.java.checkers.client.Networking;


import cz.vse.java.checkers.client.Game.Controller;
import cz.vse.java.checkers.client.Game.GameController;
import cz.vse.java.checkers.client.Game.InitialConnControler;
import cz.vse.java.checkers.client.Game.WaitingRoomController;
import cz.vse.java.checkers.common.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.vse.java.checkers.common.ServerMessage;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class SampleMessageHandler extends MessageHandler{
    Connection connection;

   private final ArrayList<Controller> controllers  = new ArrayList<>();
   private InitialConnControler initialController;
   private WaitingRoomController waitingRoomController;
   private GameController gameController;

   private ResponseManager rm = ResponseManager.getInstance();

    // Stores pending responses mapped by their unique Correlation ID
    private final Map<String, CompletableFuture<Message>> pendingRequests = new ConcurrentHashMap<>();


   private final Logger logger = LoggerFactory.getLogger(SampleMessageHandler.class);




    public SampleMessageHandler(Connection connection){
        this.connection = connection;
    }

    public void setInitialController(InitialConnControler initialController) {
        this.initialController = initialController;
    }

    public void setWaitingRoomController(WaitingRoomController waitingRoomController) {
        this.waitingRoomController = waitingRoomController;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void onConnect() {
        System.out.println("Connected");
    }


    public boolean send(ClientMessage clientMessage, String UID, String content){
        return connection.send(clientMessage, UID + " " + content);
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
            case SETUP -> setUpMatch(msg.getContent());
            case MOVED -> updateBoardState(msg.getContent());
        }
        rm.dispatchResponse(msg.getID(), msg);

    }

    public void registerController(Controller controller){
        controllers.add(controller);
    }

    private void handleOK(Message message){

    }



    private void updateWaitingRoom(String message){
        logger.info("updating players");
        String result = StringUtils.substringBetween(message, "[", "]");
        String[] players = result.split(", ");
        controllers.getFirst().updatePlayersWaiting(players);

    }

    private void updateRequestingMatches(String playerName){
        logger.info("updating requesting matches");
        String result = StringUtils.substringBetween(playerName, "[", "]");
        controllers.getFirst().updateRequestingMatches(result);

    }

    private void setUpMatch(String setupWith){
        logger.info("Setting up match with: {}", setupWith);
        String result = StringUtils.substringBetween(setupWith, "[", "]");
        controllers.getFirst().setUpMatch(result);
    }

    private void updateBoardState(String move){
        String tmp = StringUtils.substringBetween(move, "[", "]");
        logger.info("Moving opponent piece to: {}", move);
        String[] parts = tmp.split(" ");

        int[] a1 = Arrays.stream(parts[0].split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

        int[] a2 = Arrays.stream(parts[1].split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

        Pos from = new Pos(a1[0], a1[1]);
        Pos to = new Pos(a2[0], a2[1]);

        gameController.moveOpponentPiece(from, to);

    }





}
