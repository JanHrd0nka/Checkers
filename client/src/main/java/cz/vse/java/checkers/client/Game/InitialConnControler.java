package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.ServerMessage;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

public class InitialConnControler extends Controller{

    private static final Logger log = LoggerFactory.getLogger(InitialConnControler.class);
    private Connection connection = Connection.getInstance();
    public boolean requestingOK;


    Scene nextScene;

    @FXML
    private TextField nameField;
    @FXML
    private Button waitingRoomButton;


    @FXML
    private void initialize() {
        waitingRoomButton.setOnAction(event ->requestWaitingRoom());
    }


    private void requestWaitingRoom() {
        String name = nameField.getText();
        if(name.isEmpty()){
            log.info("Name field is empty!");
        } else{
            try{
                log.info("Sending login message with name: {}", name);
                boolean result = connection.send(ClientMessage.LOGIN, name);
                if(result){
                    requestingOK = true;
                    waitingRoomButton.setDisable(true);
                    while (requestingOK){

                    }
                    connection.setName(name);
                    Stage stage = (Stage)  waitingRoomButton.getScene().getWindow();
                    stage.setScene(nextScene);
                    waitingRoomButton.setDisable(false);
                }
            } catch (Exception e){
                log.info("Failed to send login message", e);
            }
        }

    }

    public void setNextScene(Scene nextScene) {
        this.nextScene = nextScene;
    }
}
