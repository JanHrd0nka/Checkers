package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.MessageEventBus;
import cz.vse.java.checkers.client.Networking.MessageListeners.SetupListener;
import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.MessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Message;
import cz.vse.java.checkers.common.ServerMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** * SetupController implementuje SetupListener. */
public class SetupController extends Controller implements SetupListener {
    private static final Logger log = LoggerFactory.getLogger(SetupController.class);

    private final MessageHandler handler = Connection.getInstance().getHandler();
    private final MessageEventBus eventBus = MessageEventBus.getInstance();

    @FXML
    private CheckBox colorBox;

    @FXML
    private CheckBox takeBox;

    @FXML
    private Button confirmBtn;

    @FXML
    private void initialize() {
        confirmBtn.setOnAction(event -> confirm());
        eventBus.registerSetupListener(this);
    }

    public void cleanup() {
        eventBus.unregisterSetupListener(this);
    }

    @Override
    public void onSetupReceived(int time, boolean isWhite, boolean mustTake) {
        Platform.runLater(() -> {
            log.info("Setup received from opponent: white={}, mustTake={}", isWhite, mustTake);
            setupGame(isWhite, mustTake);
        });
    }

    private void confirm() {
        boolean isWhite = colorBox.isSelected();
        boolean mustTake = takeBox.isSelected();
        StringBuilder sb = new StringBuilder();
        String UID = generateID();
        sb.append("600 ");
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
                                    log.info("Setup confirmed by server");
                                } else {
                                    log.error("Setup failed: {}", response.getContent());
                                    resetUI();
                                }
                            });
                        })
                        .orTimeout(5, TimeUnit.SECONDS)
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                log.error("Setup timeout");
                                confirmBtn.setDisable(false);
                            });
                            return null;
                        });
            }
        } catch (Exception e) {
            log.error("Error sending setup: ", e);
            confirmBtn.setDisable(false);
        }
    }

    private void setupGame(boolean isWhite, boolean mustTake) {
        eventBus.publishGameSetup(60, isWhite, mustTake);
        resetUI();
        Stage stage = (Stage) confirmBtn.getScene().getWindow();
        stage.setScene(getNextScene());
    }

    private void resetUI() {
        Platform.runLater(() -> {
            colorBox.setSelected(false);
            takeBox.setSelected(false);
            confirmBtn.setDisable(false);
            log.debug("Setup UI reset");
        });
    }

}
