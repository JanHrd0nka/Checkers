package cz.vse.java.checkers.client.Networking;


import cz.vse.java.checkers.client.Game.*;
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
   private SetupController setupController;
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

    public void setSetupController(SetupController setupController) {
        this.setupController = setupController;
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
        String content = msg.getContent();
        content = StringUtils.substringBetween(content, "[", "]");


        switch (ServerMessage.valueOf(msg.getToken())){
            case OK -> handleOK(msg);
            case PLAYERS_WAITING -> updateWaitingRoom(content);
            case MATCH -> updateRequestingMatches(content);
            case SETUP -> setUpMatch(content);
            case MOVED -> updateBoardState(content);
            case UNMATCH -> unmatchPlayer(content);
            case STATE -> handleState(content);
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
        String[] players = message.split(", ");
        controllers.getFirst().updatePlayersWaiting(players);

    }

    private void updateRequestingMatches(String playerName){
        logger.info("updating requesting matches");
        controllers.getFirst().updateRequestingMatches(playerName, false);

    }

    private void setUpMatch(String setupWith){
        logger.info("Setting up match with: {}", setupWith);
        controllers.getFirst().setUpMatch(setupWith);
    }

    private void updateBoardState(String move){
        logger.info("Moving opponent piece to: {}", move);
        String[] parts = move.split(" ");

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

    private void unmatchPlayer(String content){
        waitingRoomController.updateRequestingMatches(content, true);
    }

    private void handleState(String content){
        logger.info("Game set up by opponent");
        String[] parts = content.split(", ");
        boolean color = (parts[0].equals("w"));
        boolean mustTake = parts[1].equals("must");
        gameController.setWhite(color);
        gameController.setMustTake(mustTake);
        setupController.setupGame();
    }





}
