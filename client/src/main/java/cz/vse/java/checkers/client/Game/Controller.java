package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.ResponseManager;
import javafx.scene.Scene;

import java.util.UUID;

public class Controller {
    //TODO

    private Scene nextScene;
    private Scene prevScene;

    protected ResponseManager rm = ResponseManager.getInstance();

    public static String generateID() {
        return UUID.randomUUID().toString();
    }


    public void updatePlayersWaiting(String[] playersList){

    }

    public void updateRequestingMatches(String name, boolean unmatch){

    }

    public void setUpMatch(String playerName){

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
