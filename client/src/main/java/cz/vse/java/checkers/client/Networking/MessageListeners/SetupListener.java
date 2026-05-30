package cz.vse.java.checkers.client.Networking.MessageListeners;

/** * Listener pro setup zprávy */
public interface SetupListener extends MessageListener {
    void onSetupReceived(int time, boolean isWhite, boolean mustTake);
}
