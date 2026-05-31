package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.MessageEventBus;
import cz.vse.java.checkers.client.Networking.MessageListeners.SetupListener;
import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.MessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.client.Networking.Message;
import cz.vse.java.checkers.common.ServerMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Controller pro obsluhu obrazovky nastavení parametrů hry.
 *
 * @author Adam Filinger
 * @version 1.0
 *
 */
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
    private TextField timeSelector;

    @FXML
    private void initialize() {
        confirmBtn.setOnAction(event -> confirm());
        eventBus.registerSetupListener(this);
    }

    @Override
    public void onSetupReceived(int time, boolean isWhite, boolean mustTake) {
        Platform.runLater(() -> {
            log.info("Setup received from opponent: time = {}, white={}, mustTake={}", time, isWhite, mustTake);
            setupGame(time, isWhite, mustTake);
        });
    }

    /**
     * Zaslání parametrů hry na server
     */
    private void confirm() {
        String time = timeSelector.getText();
        boolean isWhite = colorBox.isSelected();
        boolean mustTake = takeBox.isSelected();
        StringBuilder sb = new StringBuilder();
        String UID = generateID();
        sb.append(time).append(" ");
        sb.append(isWhite ? "w " : "b ");
        sb.append(mustTake ? "must" : "no");

        try {
            boolean result = handler.send(ClientMessage.SETUP, UID, sb.toString());
            if (result) {
                CompletableFuture<Message> responseFuture = rm.registerRequest(UID);
                confirmBtn.setDisable(true);
                responseFuture.thenAccept(response -> Platform.runLater(() -> {
                            if (Objects.equals(response.getToken(), ServerMessage.OK.name())) {
                                log.info("Setup confirmed by server");
                            } else {
                                log.error("Setup failed: {}", response.getContent());
                                resetUI();
                            }
                        }))
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

    /**
     * Nastavení parametrů hry a přechod do herní obrazovky
     *
     * @param time
     * @param isWhite
     * @param mustTake
     */
    private void setupGame(int time, boolean isWhite, boolean mustTake) {
        eventBus.publishGameSetup(time, isWhite, mustTake);
        resetUI();
        Stage stage = (Stage) confirmBtn.getScene().getWindow();
        stage.setScene(getNextScene());
    }

    private void resetUI() {
        Platform.runLater(() -> {
            colorBox.setSelected(false);
            takeBox.setSelected(false);
            timeSelector.setText("");
            confirmBtn.setDisable(false);
            log.debug("Setup UI reset");
        });
    }

}
