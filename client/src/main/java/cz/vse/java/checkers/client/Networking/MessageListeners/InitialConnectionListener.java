package cz.vse.java.checkers.client.Networking.MessageListeners;

/** * Listener pro initial connection */
public interface InitialConnectionListener extends MessageListener {
    void onConnectionResponse(String response);
}
