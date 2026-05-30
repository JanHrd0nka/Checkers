package cz.vse.java.checkers.client.Networking;

import cz.vse.java.checkers.client.Game.GameController;
import cz.vse.java.checkers.client.Networking.MessageListeners.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/** * Centrální event bus pro publikování zpráv od serveru. * Používá observer pattern pro decoupling MessageHandler od UI komponent. */
public class MessageEventBus {
    private static MessageEventBus instance;
    private static final Logger logger = LoggerFactory.getLogger(MessageEventBus.class);

    private final Set<WaitingRoomListener> waitingRoomListeners = new CopyOnWriteArraySet<>();
    private final Set<SetupListener> setupListeners = new CopyOnWriteArraySet<>();
    private final Set<GameListener> gameListeners = new CopyOnWriteArraySet<>();
    private final Set<InitialConnectionListener> initialConnectionListeners = new CopyOnWriteArraySet<>();

    private MessageEventBus() {}

    public static MessageEventBus getInstance() {
        if (instance == null) {
            synchronized (MessageEventBus.class) {
                if (instance == null) {
                    instance = new MessageEventBus();
                }
            }
        }
        return instance;
    }

    // --- Registration methods ---

    public void registerWaitingRoomListener(WaitingRoomListener listener) {
        logger.debug("Registering WaitingRoomListener: {}", listener.getClass().getSimpleName());
        waitingRoomListeners.add(listener);
    }

    public void unregisterWaitingRoomListener(WaitingRoomListener listener) {
        logger.debug("Unregistering WaitingRoomListener: {}", listener.getClass().getSimpleName());
        waitingRoomListeners.remove(listener);
    }

    public void registerSetupListener(SetupListener listener) {
        logger.debug("Registering SetupListener: {}", listener.getClass().getSimpleName());
        setupListeners.add(listener);
    }

    public void unregisterSetupListener(SetupListener listener) {
        logger.debug("Unregistering SetupListener: {}", listener.getClass().getSimpleName());
        setupListeners.remove(listener);
    }

    public void registerGameListener(GameListener listener) {
        logger.debug("Registering GameListener: {}", listener.getClass().getSimpleName());
        gameListeners.add(listener);
    }

    public void unregisterGameListener(GameListener listener) {
        logger.debug("Unregistering GameListener: {}", listener.getClass().getSimpleName());
        gameListeners.remove(listener);
    }

    public void registerInitialConnectionListener(InitialConnectionListener listener) {
        logger.debug("Registering InitialConnectionListener: {}", listener.getClass().getSimpleName());
        initialConnectionListeners.add(listener);
    }

    public void unregisterInitialConnectionListener(InitialConnectionListener listener) {
        logger.debug("Unregistering InitialConnectionListener: {}", listener.getClass().getSimpleName());
        initialConnectionListeners.remove(listener);
    }

    // --- Publishing methods ---

    public void publishPlayersUpdated(String[] playersList) {
        logger.debug("Publishing PlayersUpdated event for {} players", playersList.length);
        waitingRoomListeners.forEach(listener -> listener.onPlayersUpdated(playersList));
    }

    public void publishRequestingMatchesUpdated(String playerName, boolean unmatched) {
        logger.debug("Publishing RequestingMatchesUpdated event for player: {}", playerName);
        waitingRoomListeners.forEach(listener -> listener.onRequestingMatchesUpdated(playerName, unmatched));
    }

    public void publishSetupMatch(String playerName) {
        logger.debug("Publishing SetupMatch event for player: {}", playerName);
        waitingRoomListeners.forEach(listener -> listener.onSetupMatch(playerName));
    }

    public void publishSetupReceived(int time, boolean isWhite, boolean mustTake) {
        logger.debug("Publishing SetupReceived event: white={}, mustTake={}", isWhite, mustTake);
        setupListeners.forEach(listener -> listener.onSetupReceived(time, isWhite, mustTake));
    }

    public void publishOpponentMoved(String boardState) {
        logger.debug("Publishing OpponentMoved event");
        gameListeners.forEach(listener -> listener.onOpponentMoved(boardState));
    }

    public void publishGameResult(String winner, String score) {
        logger.debug("Publishing GameResult event: winner={}, score={}", winner, score);
        gameListeners.forEach(listener -> listener.onGameResult(winner, score));
    }

    public void publishRematchOffer(boolean accepted) {
        logger.debug("Publishing RematchOffer event: accepted={}", accepted);
        gameListeners.forEach(listener -> listener.onRematchOffer(accepted));
    }


    public void publishConnectionResponse(String response) {
        logger.debug("Publishing ConnectionResponse event");
        initialConnectionListeners.forEach(listener -> listener.onConnectionResponse(response));
    }

    public void publishGameSetup(int i, boolean isWhite, boolean mustTake) {
        logger.debug("Publishing GameSetup event from SetupController to GameController: white={}, mustTake={}", isWhite, mustTake);
        gameListeners.forEach(listener -> listener.onGameSetup(i, isWhite, mustTake));
    }

    public void publishDrawOffer() {
        logger.debug("Publishing DrawOffer event");
        gameListeners.forEach(GameListener::onDrawOffer);
    }

    public void publishDrawDeclined() {
        logger.debug("Publishing DrawDeclined event");
        gameListeners.forEach(GameListener::onDrawDeclined);
    }

    public void publishTime(String s) {
        logger.debug("Publishing Time event: {}", s);
        gameListeners.forEach(listener -> listener.updateTime(s));
    }
}
