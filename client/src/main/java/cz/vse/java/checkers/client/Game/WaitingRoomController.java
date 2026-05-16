package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.common.ClientMessage;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.effect.SepiaTone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WaitingRoomController {

    private static final Logger log = LoggerFactory.getLogger(WaitingRoomController.class);
    private Scene nextScene;

    private Connection connection = Connection.getInstance();

    private static Set<String> players = new HashSet<>();


    @FXML
    private ListView<String> availablePlayersList;

    @FXML
    private ListView<String> playersRequestingList;

    @FXML
    private void initialize() {
        availablePlayersList.setOnMouseClicked(event -> requestMatch(availablePlayersList.getSelectionModel().getSelectedItem()));
        playersRequestingList.setOnMouseClicked(event -> acceptMatch());

    }


    public void updatePlayersWaiting(String[] playersList){
        players.addAll(Arrays.asList(playersList));
        if(availablePlayersList != null){
            availablePlayersList.getItems().clear();
            for (String name : players){
                availablePlayersList.getItems().add(name);
            }
        }
    }

    public void updateRequestingMatches(String name){
        if(playersRequestingList != null){
            playersRequestingList.getItems().add(name);
        }
    }



    public void setNextScene(Scene nextScene) {
        this.nextScene = nextScene;
    }

    private void acceptMatch() {

    }

    private void requestMatch(String playerName) {
        if(connection.send(ClientMessage.MATCH, playerName)){
            log.info("Sent match request to: {}", playerName);
        } else{
            if(players.contains(playerName)){
                log.info("Player {} exists but request match failed to send", playerName);
            } else{
                log.info("Player {} doesn't exist. Can't send a match request", playerName);
            }


        }
    }



}
