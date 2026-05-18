package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.ResponseManager;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Message;
import cz.vse.java.checkers.common.ServerMessage;
import javafx.application.Application;
import javafx.application.Platform;
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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class InitialConnControler extends Controller{

    private static final Logger log = LoggerFactory.getLogger(InitialConnControler.class);
    private Connection connection = Connection.getInstance();

    private ResponseManager rm = ResponseManager.getInstance();


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
                String UID = generateID();
                boolean result = connection.send(ClientMessage.LOGIN, UID + " " + name);
                if(result){
                    CompletableFuture<Message> responseFuture = rm.registerRequest(UID);
                    waitingRoomButton.setDisable(true);
                    // 3. Handle the response whenever it arrives without blocking the UI
                    responseFuture.thenAccept(response -> {
                                connection.setName(name);
                                // CRITICAL: GUI updates must happen on the main UI thread
                                Platform.runLater(() -> {
                                    if (Objects.equals(response.getToken(), ServerMessage.OK.name())) {
                                        Stage stage = (Stage)  waitingRoomButton.getScene().getWindow();
                                        stage.setScene(nextScene);
                                        waitingRoomButton.setDisable(false);
                                    } else {
                                        waitingRoomButton.setDisable(false);
                                        log.error("Invalid credentials");
                                    }
                                });

                            }).orTimeout(5, TimeUnit.SECONDS) // Avoid waiting forever if server goes down
                            .exceptionally(ex -> {
                                Platform.runLater(() -> log.error("Network timeout while waiting for response from server."));
                                return null;
                            });
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
