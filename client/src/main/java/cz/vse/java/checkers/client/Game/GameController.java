package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Message;
import cz.vse.java.checkers.client.Networking.MessageListeners.GameListener;
import cz.vse.java.checkers.client.Networking.MessageEventBus;
import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.MessageHandler;
import cz.vse.java.checkers.common.*;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * GameController - spravuje herní logiku a interakci se serverem.
 * Implementuje GameListener pro příjem event notifikací.
 * Oddělena logika pro game state (GameStateManager) a scény (SceneNavigator).
 *
 * @author Adam Filinger
 * @version 1.0
 *
 */
public class GameController extends Controller implements GameListener {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private static final int NETWORK_TIMEOUT_SECONDS = 5;

    private final MessageHandler handler;
    private final GameStateManager gameStateManager;
    private final SceneNavigator sceneNavigator;

    private final BoardController board;

    // Herní stav
    private int selectedRow = -1;
    private int selectedCol = -1;

    /**
     * Konstruktor - GameController je nyní bez přímé závislosti na MessageHandler
     */
    public GameController(BoardController board) {
        this.board = board;
        this.handler = Connection.getInstance().getHandler();
        MessageEventBus eventBus = MessageEventBus.getInstance();
        this.gameStateManager = new GameStateManager();
        this.sceneNavigator = new SceneNavigator(board, this);

        eventBus.registerGameListener(this);
    }

    // ===== METHOD DELEGATION =====

    public void setupNewGame(boolean isWhite, boolean mustTake) {
        gameStateManager.createNewGame(isWhite, mustTake);
        board.resetBoardDisplay();
    }

    public boolean isWhite() {
        return gameStateManager.isWhite();
    }

    public boolean isPlayerTurn() {
        return gameStateManager.isWhiteToMove() == gameStateManager.isWhite();
    }

    public Game getGame() {
        return gameStateManager.getGame();
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


    // ===== NETWORK OPERATIONS =====

    /**
     * Odeslat hermický tah na server
     */
    public void movePiece(Pos from, Pos to) {
        if (from == null || to == null) {
            logger.warn("Invalid move: from or to position is null");
            return;
        }

        String UID = generateID();
        String moveData = formatMoveData(from, to);

        if (!handler.send(ClientMessage.MOVE, UID, moveData)) {
            logger.error("Failed to send move to server");
            return;
        }

        // Registrovat očekávaní odpovědi a poslat zprávu
        sendNetworkMessage(UID, response -> {
            if (isSuccessResponse(response)) {
                gameStateManager.makeMove(from, to);
                updateBoardDisplay();
                logger.info("Move successful");
            } else {
                logger.warn("Invalid move");
            }
        });
    }

    /**
     * Obdržena zpráva o pohybu soupeře
     */
    @Override
    public void onOpponentMoved(String boardState) {
        Platform.runLater(() -> {
            logger.info("Opponent moved, updating board");
            gameStateManager.updateBoard(boardState);
            updateBoardDisplay();
        });
    }

    /**
     * Obdržena zpráva o výsledku hry
     */
    @Override
    public void onGameResult(String ID, String winner, String score) {
        Platform.runLater(() -> {
            String currentPlayerName = Connection.getInstance().getName();
            boolean isWin = Objects.equals(winner, currentPlayerName);
            board.showResultDialog(ID, isWin, score);
        });
    }

    /**
     * Obdržena nabídka na rematch
     */
    @Override
    public void onRematchOffer(boolean accepted) {
        Platform.runLater(() -> {
            if (accepted) {
                logger.info("Opponent accepted rematch");
                //setupNewGame();
                sceneNavigator.navigateToSetup();
            } else {
                Platform.runLater(() -> {
                    logger.info("Opponent declined rematch - returning to waiting room");
                    board.closeResultDialog();
                    returnToWaitingRoom();
                });
            }
        });
    }

    @Override
    public void onGameSetup(int i, boolean isWhite, boolean mustTake) {
        Platform.runLater(() -> {
            logger.info("Game setup received: time={}, isWhite={}, mustTake={}", i, isWhite, mustTake);
            board.createNewGame(isWhite, mustTake);
        });
    }

    @Override
    public void onDrawOffer() {
        Platform.runLater(() -> {
            logger.info("Received draw offer from opponent, displaying draw alert");
            board.displayDraw();
        });
    }

    @Override
    public void onDrawDeclined() {
        Platform.runLater(() -> {
            logger.info("Opponent declined draw offer, displaying declined alert");
            board.drawDeclined();
        });
    }

    @Override
    public void updateTime(String s) {
        Platform.runLater(() -> {
            logger.info("Received time update from server: {}", s);
            board.updateTime(s);
        });
    }


    /**
     * Vrátit se do waiting roomu
     */
    public void returnToWaitingRoom() {
        // setupNewGame(); // Reset hry

        String name = Connection.getInstance().getName();
        String UID = generateID();

        if (!handler.send(ClientMessage.JOIN_WAITING_ROOM, UID, name)) {
            logger.error("Failed to send join waiting room message");
            return;
        }

        sendNetworkMessage(UID, response -> {
            if (isSuccessResponse(response)) {
                logger.info("Successfully joined waiting room");
                sceneNavigator.navigateToWaitingRoom();
            } else {
                logger.error("Failed to join waiting room");
            }
        });
    }

    /**
     * Poslat rematch nabídku
     */
    public void sendRematchMessage(ClientMessage token, String UID, String message) {
        if (!handler.send(token, UID, message)) {
            logger.error("Failed to send rematch message");
            return;
        }

        sendNetworkMessage(UID, response -> {
            if (isSuccessResponse(response)) {
                String content = response.getContent();
                if ("[to-WR]".equals(content)) {
                    returnToWaitingRoom();
                } else {
                    logger.debug("Rematch response: {}", content);
                }
            } else {
                logger.error("Rematch request failed");
            }
        });

    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Formátovat data pohybu
     */
    private String formatMoveData(Pos from, Pos to) {
        return String.valueOf(from.x()) + from.y() + to.x() + to.y();
    }

    /**
     * Kontrola, zda je odpověď úspěšná
     */
    private boolean isSuccessResponse(Message response) {
        return Objects.equals(response.getToken(), ServerMessage.OK.name());
    }

    /**
     * Poslat síťovou zprávu s timeoutem a error handling     * Centrální místo pro všechny síťové operace
     */
    private void sendNetworkMessage(String UID, java.util.function.Consumer<Message> onSuccess) {
        CompletableFuture<Message> responseFuture = rm.registerRequest(UID);

        responseFuture
                .orTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .thenAccept(response -> Platform.runLater(() -> onSuccess.accept(response)))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            logger.error("Network timeout after {} seconds", NETWORK_TIMEOUT_SECONDS)
                    );
                    return null;
                });
    }

    /**
     * Aktualizovat zobrazení desky
     */
    private void updateBoardDisplay() {
        Platform.runLater(board::drawPieces);
    }

    public void sendForfeitMessage(ClientMessage clientMessage, String uid, String s) {
        if (!handler.send(clientMessage, uid, s)) {
            logger.error("Failed to send foresight message");
        } else {
            logger.info("Sent forfeit message");
            sendNetworkMessage(uid, response -> {
                if (isSuccessResponse(response)) {
                    logger.debug("Successful server response on forfeit message");
                } else {
                    logger.warn("Server didn't respond to forfeit message");
                }
            });
        }
    }

    public void sendDrawRequest(ClientMessage clientMessage, String uid, String s) {
        if (!handler.send(clientMessage, uid, s)) {
            logger.error("Failed to send draw request message");
        } else {
            logger.info("Sent draw request message");
            sendNetworkMessage(uid, response -> {
                if (isSuccessResponse(response)) {
                    logger.debug("Successful server response on draw request message");
                } else {
                    logger.warn("Server didn't respond to draw request message");
                }
            });
        }
    }

    public void sendDrawOffer(boolean b) {
        String UID = generateID();
        String message = b ? "accept" : "decline";
        if (!handler.send(ClientMessage.DRAW, UID, message)) {
            logger.error("Failed to send draw response");
        } else {
            sendNetworkMessage(UID, response -> {
                if (isSuccessResponse(response)) {
                    logger.debug("Successful server response on draw offer: {}", message);
                } else {
                    logger.warn("Server didn't respond to draw offer: {}", message);
                }
            });
        }
    }

}