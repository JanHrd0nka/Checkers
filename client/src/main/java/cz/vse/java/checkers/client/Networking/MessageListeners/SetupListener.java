package cz.vse.java.checkers.client.Networking.MessageListeners;

/**
 * Listener pro setup zprávy
 *
 * @author Adam Filinger
 * @version 1.0
 *
 */
public interface SetupListener extends MessageListener {
    void onSetupReceived(int time, boolean isWhite, boolean mustTake);
}
