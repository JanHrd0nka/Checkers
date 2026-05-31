package cz.vse.java.checkers.client.Networking.MessageListeners;

/** * Listener pro game state
 * @author Adam Filinger
 * @version 1.2
 * */
public interface GameListener extends MessageListener {
    void onOpponentMoved(String boardState);
    void onGameResult(String ID, String winner, String score);
    void onRematchOffer(boolean accepted);
    void onGameSetup(int i, boolean isWhite, boolean mustTake);

    void onDrawOffer();

    void onDrawDeclined();

    void updateTime(String s);
}
