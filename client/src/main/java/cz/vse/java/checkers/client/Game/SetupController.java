package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.ResponseManager;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Message;
import cz.vse.java.checkers.common.ServerMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SetupController extends Controller{

    private static final Logger log = LoggerFactory.getLogger(SetupController.class);
    private SampleMessageHandler handler;

    private ResponseManager rm = ResponseManager.getInstance();


    Scene nextScene;

    @FXML
    private CheckBox colorBox;
    @FXML
    private CheckBox takeBox;
    @FXML
    private Button confirmBtn;


    @FXML
    private void initialize() {
        handler = Connection.getInstance().getHandler();
        confirmBtn.setOnAction(event -> confirm());
    }

    public void confirm(){
        boolean isWhite = colorBox.isSelected();
        boolean mustTake = takeBox.isSelected();
        StringBuilder sb = new StringBuilder();
        String UID = generateID();
        sb.append("60 ");
        sb.append(isWhite ? "w " : "b ");
        sb.append(mustTake ? "must" : "no");
        try {
            boolean result = handler.send(ClientMessage.SETUP, UID, sb.toString());
            if (result) {
                CompletableFuture<Message> responseFuture = rm.registerRequest(UID);
                confirmBtn.setDisable(true);
                responseFuture.thenAccept(response -> {
                            Platform.runLater(() -> {
                                if (Objects.equals(response.getToken(), ServerMessage.OK.name())) {
                                    log.info("Setup successful, moving to game scene.");
                                    setupGame(isWhite, mustTake);
                                } else {
                                    log.info("Setup failed: " + response.getContent());
                                }

                            });
                        }).orTimeout(5, TimeUnit.SECONDS) // Avoid waiting forever if server goes down
                        .exceptionally(ex -> {
                            Platform.runLater(() -> log.error("Network timeout while waiting for response from server."));
                            return null;
                        });

                colorBox.setSelected(false);
                takeBox.setSelected(false);
                confirmBtn.setDisable(false);
            }
        } catch (Exception e) {
            log.error("Error sending setup message: ", e);
        }




    }

    public void setupGame(boolean isWhite, boolean mustTake){
        handler.getGameController().setWhite(isWhite);
        handler.getGameController().setMustTake(mustTake);
        Stage stage = (Stage) confirmBtn.getScene().getWindow();
        stage.setScene(nextScene);
    }


    public void setNextScene(Scene nextScene) {
        this.nextScene = nextScene;
    }
}
