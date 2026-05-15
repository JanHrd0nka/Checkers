package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameController {

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    private ConnectionController connectionController;

    private Game game;

    // Variables to remember which piece the user clicked
    private int selectedRow = -1;
    private int selectedCol = -1;

    public GameController(ConnectionController connectionController, boolean mustTake) {
        this.connectionController = connectionController;
        this.game = new Game(mustTake);
    }


    public void setupStartingPositions() {
        game.setPieces();
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

    public boolean send(ClientMessage clientMessage, String text) {
        return connectionController.send(clientMessage, text);
    }

}