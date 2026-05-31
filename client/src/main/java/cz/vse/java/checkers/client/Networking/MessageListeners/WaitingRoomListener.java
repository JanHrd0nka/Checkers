package cz.vse.java.checkers.client.Networking.MessageListeners;

/**
 * Listener pro waiting room aktualizace
 *
 * @author Adam Filinger
 * @version 1.0
 *
 */
public interface WaitingRoomListener extends MessageListener {
    void onPlayersUpdated(String[] playersList);

    void onRequestingMatchesUpdated(String playerName, boolean unmatched);

    void onSetupMatch(String playerName);
}
