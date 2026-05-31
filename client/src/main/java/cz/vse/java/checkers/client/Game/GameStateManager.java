package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.common.Game;
import cz.vse.java.checkers.common.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * GameStateManager - odděluje herní logiku a stav od UI kontrolery.
 * Jedinou odpovědností je spravovat stav Game2 objektu.
 *
 * @author Adam Filinger
 * @version 1.0
 *
 */
public class GameStateManager {
    private static final Logger logger = LoggerFactory.getLogger(GameStateManager.class);

    private Game game;
    private boolean isWhite;

    public GameStateManager() {
    }

    /**
     * Vytvořit novou hru (ideálně s konfiguracemi ze serveru)
     */
    public void createNewGame(boolean isWhite, boolean mustTake) {
        // Zde by měly přijít parametry ze serveru (čas, pravidla, počáteční pozice)
        setIsWhite(isWhite);
        this.game = new Game(mustTake);
        logger.info("New game created");
    }

    /**
     * Provést tah
     */
    public void makeMove(Pos from, Pos to) {
        List<Pos> path = new ArrayList<>();
        path.add(from);
        path.add(to);
        game.makeMove(path);
        logger.debug("Move executed: {} -> {}", from, to);
    }

    /**
     * Aktualizovat stav desky z obsahu serveru
     */
    public void updateBoard(String boardState) {
        try {
            game.updateBoard(boardState);
            logger.info("Board updated from server");
        } catch (Exception e) {
            logger.error("Failed to update board", e);
        }
    }


    public boolean isWhiteToMove() {
        return game.isWhiteToMove();
    }

    public Game getGame() {
        return game;
    }

    public void setIsWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhite() {
        return isWhite;
    }

}
