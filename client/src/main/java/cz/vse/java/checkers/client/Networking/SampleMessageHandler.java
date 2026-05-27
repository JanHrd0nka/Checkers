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
        String[] content = StringUtils.substringBetween(msg.getContent(), "[", "]").split(", ");
        String ID = msg.getID();


        switch (ServerMessage.valueOf(msg.getToken())){
            case OK -> handleOK(msg);
            case PLAYERS_WAITING -> updateWaitingRoom(content);
            case MATCH -> updateRequestingMatches(content);
            case SETUP -> setUpMatch(content);
            case UNMATCH -> unmatchPlayer(content);
            case STATE -> handleState(content, ID);
            case RESULT -> handleResult(content);
            case REMATCH -> handleRematch(ID);
        }
        rm.dispatchResponse(ID, msg);

    }

    public void registerController(Controller controller){
        controllers.add(controller);
    }

    private void handleOK(Message message){

    }



    private void updateWaitingRoom(String[] message){
        logger.info("updating players");
        waitingRoomController.updatePlayersWaiting(message);

    }

    private void updateRequestingMatches(String[] playerName){
        logger.info("updating requesting matches");
        if(playerName.length > 0) {
            waitingRoomController.updateRequestingMatches(playerName[0], false);
        }

    }

    private void setUpMatch(String[] setupWith){
        logger.info("Setting up match with: {}", setupWith);
        if(setupWith.length > 0) {
            waitingRoomController.setUpMatch(setupWith[0]);
        }
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

    private void unmatchPlayer(String[] content){
        waitingRoomController.updateRequestingMatches(content[0], true);
    }

    private void handleState(String[] content, String ID){
        logger.info("Game set up by opponent");
        if(ID.equalsIgnoreCase("match-setup")){
            int time = Integer.parseInt(content[0]);
            boolean isWhite = content[1].equalsIgnoreCase("true");
            boolean mustTake = content[2].equalsIgnoreCase("true");
            setupController.setupGame(isWhite, mustTake);
        } else if (ID.equalsIgnoreCase("op-moved")) {
            gameController.updateBoard(content[0]);
        }
    }

    private void handleResult(String[] content){
        boolean isWin = content[0].equals(connection.getName());
        String score = content[1];
        gameController.showResult(isWin, score);
    }

    private void handleRematch(String content){
        logger.info("Opponent offered rematch: {}", content);
        // content je např. "[opponentName]" (záleží na formátu), zde můžeme:
        // - logovat,
        // - pokud jste v Game scéně, notify BoardController/GameController,
        // - nebo zprostředkovat přes waitingRoomController jako updateRequestingMatches.
        // Pro jednoduchost:
        //waitingRoomController.updateRequestingMatches(content, false);
        if(Objects.equals(content, "accepted")){
            gameController.sendToSetup();
        }
    }

    public GameController getGameController(){
        return gameController;
    }





}
