package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.common.Game2;
import cz.vse.java.checkers.common.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** * GameStateManager - odděluje herní logiku a stav od UI kontrolery. * Jedinou odpovědností je spravovat stav Game2 objektu. */
public class GameStateManager {
    private static final Logger logger = LoggerFactory.getLogger(GameStateManager.class);

    private Game2 game;
    private boolean isWhite;
    private boolean mustTake = true;

    public GameStateManager() {
        this.game = new Game2(mustTake);
    }

    /**     * Vytvořit novou hru (ideálně s konfiguracemi ze serveru)     */
    public void createNewGame() {
        // Zde by měly přijít parametry ze serveru (čas, pravidla, počáteční pozice)
        // Zatím test pozice:
        this.game = new Game2("""
            0 0 0 0 0 0 0 0
            0 0 4 0 3 0 0 0
            0 0 0 0 0 0 0 0
            0 0 4 0 3 0 0 0
            0 0 0 2 0 0 0 0
            0 0 0 0 0 0 0 0
            0 0 0 0 0 0 0 0
            0 0 0 0 0 0 0 0
            """.replace("\n", "").replace(" ", ""));
        logger.info("New game created");
    }

    /**     * Provést tah     */
    public void makeMove(Pos from, Pos to) {
        List<Pos> path = new ArrayList<>();
        path.add(from);
        path.add(to);
        game.makeMove(path);
        logger.debug("Move executed: {} -> {}", from, to);
    }

    /**     * Aktualizovat stav desky z obsahu serveru     */
    public void updateBoard(String boardState) {
        try {
            game.updateBoard(boardState);
            logger.info("Board updated from server");
        } catch (Exception e) {
            logger.error("Failed to update board", e);
        }
    }

    /**     * Zjistit, zda je bílý na tahu     */
    public boolean isWhiteToMove() {
        return game.isWhiteToMove();
    }

    /**     * Getter pro Game2     */
    public Game2 getGame() {
        return game;
    }

    public void setIsWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setMustTake(boolean mustTake) {
        this.mustTake = mustTake;
    }
}
