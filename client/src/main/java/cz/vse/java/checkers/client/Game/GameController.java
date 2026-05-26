package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.ResponseManager;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameController extends Controller{

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final ResponseManager rm = ResponseManager.getInstance();


    private Game2 game;
    private SampleMessageHandler handler;
    private BoardController board;
    public boolean isWhite;
    private boolean mustTake = true;

    private Scene WR;


    // Variables to remember which piece the user clicked
    private int selectedRow = -1;
    private int selectedCol = -1;

    public GameController(BoardController board) {
        setupNewGame();
        this.board = board;
        this.handler = Connection.getInstance().getHandler();
    }

    protected void setupNewGame(){
        //this.game = new Game2(mustTake);
        //test

        this.game = new Game2("""
        0 0 0 0 0 0 0 0
        0 0 4 0 3 0 0 0
        0 0 0 0 0 0 0 0
        0 0 4 0 3 0 0 0
        0 0 0 2 0 0 0 0
        0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0
        """.replace("\n", "").replace(" ", ""));
    }


    public void setWhite(boolean white) {
        isWhite = white;
    }

    public void setMustTake(boolean mustTake) {
        this.mustTake = mustTake;
    }

    public void movePiece(Pos from, Pos to){
        if(from != null && to != null){
            String UID = generateID();
            boolean result = handler.send(ClientMessage.MOVE, UID, from.x() + ""+ from.y() +
                    "" + to.x() + "" + to.y());
            if(result){
                CompletableFuture<Message> responseFuture = rm.registerRequest(UID);
                responseFuture.thenAccept(response -> {
                            // CRITICAL: GUI updates must happen on the main UI thread
                            Platform.runLater(() -> {
                                if (Objects.equals(response.getToken(), ServerMessage.OK.name())) {
                                    List<Pos> path = new ArrayList<>();
                                    path.add(from);
                                    path.add(to);
                                    game.makeMove(path);
                                    logger.info("Move successful");
                                } else {
                                    logger.info("Invalid move");
                                }
                                board.drawPieces();
                            });

                        }).orTimeout(5, TimeUnit.SECONDS) // Avoid waiting forever if server goes down
                        .exceptionally(ex -> {
                            Platform.runLater(() -> logger.error("Network timeout while waiting for response from server on move command."));
                            return null;
                        });


            }
        }
    }

    public void moveOpponentPiece(Pos from, Pos to){
        if(from != null && to != null){
            List<Pos> path = new ArrayList<Pos>();
            path.add(from);
            path.add(to);
            game.makeMove(path);
            board.drawPieces();
        }
    }

    public boolean isPlayerTurn(){
        return game.isWhiteToMove() == isWhite;
    }


    // 3. Read the data array and draw the pieces (The View)


    public void updateBoard(String content){
        game.updateBoard(content);
        board.drawPieces();
    }


    public Game2 getGame() {
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


    public void showResult(boolean isWin) {
        board.showResultDialog(isWin);
    }

    public void returnToWaitingRoom() {
        board.createNewGame();
        try{
            String name = Connection.getInstance().getName();
            logger.info("Sending join waiting room message with name: {}", name);
            String UID = generateID();
            boolean result = handler.send(ClientMessage.JOIN_WAITING_ROOM, UID, name);
            if(result){
                CompletableFuture<Message> responseFuture = rm.registerRequest(UID);
                // 3. Handle the response whenever it arrives without blocking the UI
                responseFuture.thenAccept(response -> {
                            // CRITICAL: GUI updates must happen on the main UI thread
                            Platform.runLater(() -> {
                                if (Objects.equals(response.getToken(), ServerMessage.OK.name())) {
                                    Stage stage = (Stage) board.getScene();
                                    stage.setScene(WR);
                                } else {
                                    logger.error("Invalid credentials");
                                }
                            });

                        }).orTimeout(5, TimeUnit.SECONDS) // Avoid waiting forever if server goes down
                        .exceptionally(ex -> {
                            Platform.runLater(() -> logger.error("Network timeout while waiting for response from server on join waiting room command."));
                            return null;
                        });
            }
        } catch (Exception e){
            logger.info("Failed to send join waiting room message", e);
        }
    }


    public void setWR(Scene wr){
        if(wr != null){
            this.WR = wr;
        }
    }


}