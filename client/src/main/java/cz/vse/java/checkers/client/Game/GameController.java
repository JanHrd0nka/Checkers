package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.ResponseManager;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.*;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameController extends Controller{

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    private ResponseManager rm = ResponseManager.getInstance();


    private Game game;
    private SampleMessageHandler handler;
    private BoardController board;
    private boolean isWhite;
    private boolean mustTake;


    // Variables to remember which piece the user clicked
    private int selectedRow = -1;
    private int selectedCol = -1;

    public GameController(BoardController board) {
        this.game = new Game(mustTake);
        this.board = board;
        this.handler = Connection.getInstance().getHandler();
    }


    public void setWhite(boolean white) {
        isWhite = white;
    }

    public void setMustTake(boolean mustTake) {
        this.mustTake = mustTake;
    }

    public void setupStartingPositions() {
        logger.info("Setting up starting position.");
        game.setPieces();
    }

    public void movePiece(Pos from, Pos to){
        if(from != null && to != null){
            String UID = generateID();
            boolean result = handler.send(ClientMessage.MOVE, UID, from.x() + "," + from.y() +
                    " " + to.x() + "," + to.y());
            if(result){
                CompletableFuture<Message> responseFuture = rm.registerRequest(UID);
                responseFuture.thenAccept(response -> {
                            // CRITICAL: GUI updates must happen on the main UI thread
                            Platform.runLater(() -> {
                                if (Objects.equals(response.getToken(), ServerMessage.OK.name())) {
                                    game.movePiece(from, to);
                                } else {
                                    logger.info("Invalid move");
                                }
                                board.drawPieces();
                            });

                        }).orTimeout(5, TimeUnit.SECONDS) // Avoid waiting forever if server goes down
                        .exceptionally(ex -> {
                            Platform.runLater(() -> logger.error("Network timeout while waiting for response from server."));
                            return null;
                        });


            }
        }
    }

    public void moveOpponentPiece(Pos from, Pos to){
        if(from != null && to != null){
            game.movePiece(from, to);
            board.drawPieces();
        }
    }

    public boolean isPlayerTurn(){
        return game.getWhiteToMove() == isWhite;
    }


    // 3. Read the data array and draw the pieces (The View)



    public Game getGame() {
        return game;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public void setSelectedCol(int selectedCol) {
        this.selectedCol = selectedCol;
    }


}