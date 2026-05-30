package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.MessageEventBus;
import cz.vse.java.checkers.client.Networking.MessageListeners.InitialConnectionListener;
import cz.vse.java.checkers.client.Networking.ResponseManager;
import cz.vse.java.checkers.client.Networking.MessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Message;
import cz.vse.java.checkers.common.ServerMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class InitialConnControler extends Controller implements InitialConnectionListener {

    private static final Logger log = LoggerFactory.getLogger(InitialConnControler.class);
    private MessageHandler handler;
    private final MessageEventBus eventBus;
    private Stage stage;
    private Scene thisScene;

    @FXML
    private TextField nameField;
    @FXML
    private Button waitingRoomButton;
    @FXML
    private Label errorLbl;

    public InitialConnControler() {
        this.eventBus = MessageEventBus.getInstance();
        eventBus.registerInitialConnectionListener(this);
    }

    @FXML
    private void initialize() {
        waitingRoomButton.setOnAction(event ->requestWaitingRoom());
        handler = Connection.getInstance().getHandler();
    }


    private void requestWaitingRoom() {
        String name = nameField.getText();
        if(name.isEmpty()){
            log.info("Name field is empty!");
        } else{
            try{
                log.info("Sending login message with name: {}", name);
                String UID = generateID();
                boolean result = handler.send(ClientMessage.LOGIN, UID, name);
                if(result){
                    CompletableFuture<Message> responseFuture = rm.registerRequest(UID);
                    waitingRoomButton.setDisable(true);
                    // 3. Handle the response whenever it arrives without blocking the UI
                    responseFuture.thenAccept(response -> {
                                Connection.getInstance().setName(name);
                                // CRITICAL: GUI updates must happen on the main UI thread
                                Platform.runLater(() -> {
                                    if (Objects.equals(response.getToken(), ServerMessage.OK.name())) {
                                        Stage stage = (Stage)  waitingRoomButton.getScene().getWindow();
                                        stage.setScene(getNextScene());
                                        waitingRoomButton.setDisable(false);
                                        errorLbl.setVisible(false);
                                    } else {
                                        waitingRoomButton.setDisable(false);
                                        errorLbl.setVisible(true);
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

    @Override
    public void onServerDisconnected(){
        log.debug("Initializing aletr window for server disconnected");
        Platform.runLater(() -> {
            Alert serverDisconnected = new Alert(Alert.AlertType.ERROR, "Server disconnected");

            ButtonType reconnect = new ButtonType("Reconnect to server");
            ButtonType close = new ButtonType("Close game");
            serverDisconnected.getButtonTypes().setAll(reconnect, close);
            Optional<ButtonType> result = serverDisconnected.showAndWait();
            if (result.isPresent()) {
                if (result.get() == reconnect) {
                    Connection.getInstance().connect("localhost", 5000);
                    stage.setScene(thisScene);
                } else if (result.get() == close) {
                    stage.close();
                    System.exit(0);
                }
            }

        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setThisScene(Scene thisScene) {
        this.thisScene = thisScene;
    }
}
