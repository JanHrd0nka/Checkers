package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.ResponseManager;
import javafx.scene.Scene;

import java.util.UUID;

/**
 * Rodičovská třída pro všechny controllery.
 * @author Adam Filinger
 * @version 1.0
 */
public class Controller {

    private Scene nextScene;
    private Scene prevScene;

    protected final ResponseManager rm = ResponseManager.getInstance();

    public static String generateID() {
        return UUID.randomUUID().toString();
    }


    public void setNextScene(Scene nextScene) {
        this.nextScene = nextScene;
    }

    public Scene getNextScene() {
        return nextScene;
    }

    public Scene getPrevScene() {
        return prevScene;
    }

    public void setPrevScene(Scene prevScene) {
        this.prevScene = prevScene;
    }
}
