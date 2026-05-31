package cz.vse.java.checkers.client.Networking.MessageListeners;

/** * Listener pro initial connection
 * @author Adam Filinger
 * @version 1.2
 * */
public interface InitialConnectionListener extends MessageListener {
    void onServerDisconnected();
}
