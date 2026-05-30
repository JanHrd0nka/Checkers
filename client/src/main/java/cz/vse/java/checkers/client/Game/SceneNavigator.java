package cz.vse.java.checkers.client.Game;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** * SceneNavigator - centralizuje navigaci mezi scénami. * Odděluje scény od herní logiky. */
public class SceneNavigator {
    private static final Logger logger = LoggerFactory.getLogger(SceneNavigator.class);

    private final BoardController boardController;
    private final GameController gameController;

    public SceneNavigator(BoardController boardController, GameController gameController) {
        this.boardController = boardController;
        this.gameController = gameController;
    }

    /**     * Navigovat do Setup scény     */
    public void navigateToSetup() {
        try {
            Stage stage = getStage();
            stage.setScene(gameController.getPrevScene());
            logger.info("Navigated to setup scene");
        } catch (Exception e) {
            logger.error("Failed to navigate to setup", e);
        }
    }

    /**     * Navigovat do WaitingRoom scény     */
    public void navigateToWaitingRoom() {
        try {
            Stage stage = getStage();
            stage.setScene(gameController.getNextScene());
            logger.info("Navigated to waiting room scene");
        } catch (Exception e) {
            logger.error("Failed to navigate to waiting room", e);
        }
    }

    /**     * Získat stage     */
    private Stage getStage() {
        return boardController.getStage();
    }
}