package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ListView;

public class WaitingRoomController {

    private Scene nextScene;

    private Connection connection = Connection.getInstance();

    @FXML
    private ListView<String> availablePlayersList;

    @FXML
    private ListView<String> playersRequestingList;

    @FXML
    private void initialize() {

        availablePlayersList.setOnMouseClicked(event -> requestMatch());
        playersRequestingList.setOnMouseClicked(event -> acceptMatch());
    }


    public void updatePlayersWaiting(String players){
        if(availablePlayersList != null){
            availablePlayersList.getItems().clear();
            availablePlayersList.getItems().add("New player");
        }
    }


    public void setNextScene(Scene nextScene) {
        this.nextScene = nextScene;
    }

    private void acceptMatch() {

    }

    private void requestMatch() {

    }



}
