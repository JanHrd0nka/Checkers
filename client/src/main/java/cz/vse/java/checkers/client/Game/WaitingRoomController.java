package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.ResponseManager;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Message;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.effect.SepiaTone;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WaitingRoomController extends Controller{

    private static final Logger log = LoggerFactory.getLogger(WaitingRoomController.class);
    private Scene nextScene;

    private ResponseManager rm = ResponseManager.getInstance();

    private SampleMessageHandler handler = Connection.getInstance().getHandler();

    private Set<String> playersAvailable = new HashSet<>();
    private Set<String> requestedMatches = new HashSet<>();
    private Set<String> requestingMatches = new HashSet<>();


    @FXML
    private ListView<String> availablePlayersList;

    @FXML
    private ListView<String> playersRequestingList;

    @FXML
    private void initialize() {
        availablePlayersList.setOnMouseClicked(event -> requestMatch(availablePlayersList.getSelectionModel().getSelectedItem()));
        playersRequestingList.setOnMouseClicked(event -> acceptMatch(playersRequestingList.getSelectionModel().getSelectedItem()));

    }

    @Override
    public void updatePlayersWaiting(String[] playersList){
        playersAvailable.addAll(Arrays.asList(playersList));
        if(availablePlayersList != null){
            availablePlayersList.getItems().clear();
            for (String name : playersAvailable){
                if(!Objects.equals(name, Connection.getInstance().getName())){
                    if(!requestingMatches.contains(name)){
                        if(!requestedMatches.contains(name)){
                            availablePlayersList.getItems().add(name);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateRequestingMatches(String name, boolean unmatch){
        if(playersRequestingList != null){
            if(!Objects.equals(name, Connection.getInstance().getName())){
                if (unmatch){
                 requestingMatches.remove(name);
                 playersRequestingList.getItems().remove(name);
                } else{
                    requestingMatches.add(name);
                    playersRequestingList.getItems().add(name);
                    availablePlayersList.getItems().remove(name);
                }
            }
        }
    }



    public void setNextScene(Scene nextScene) {
        this.nextScene = nextScene;
    }

    private void acceptMatch(String playerName) {
        String UID = generateID();
        if(handler.send(ClientMessage.MATCH,UID, playerName)){
            log.info("Accepted match from: {}", playerName);
        }

    }

    private void requestMatch(String playerName) {
        String UID = generateID();
        if(handler.send(ClientMessage.MATCH,UID, playerName)){
            log.info("Sent match request to: {}", playerName);
            requestedMatches.add(playerName);
            availablePlayersList.getItems().remove(playerName);
        } else{
            if(playersAvailable.contains(playerName)){
                log.info("Player {} exists but request match failed to send", playerName);
            } else{
                log.info("Player {} doesn't exist. Can't send a match request", playerName);
            }


        }
    }

    @Override
    public void setUpMatch(String playerName){
        Stage stage = (Stage)  availablePlayersList.getScene().getWindow();
        stage.setScene(nextScene);
    }



}
