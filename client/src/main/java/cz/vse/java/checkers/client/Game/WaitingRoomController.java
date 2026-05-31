package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.MessageEventBus;
import cz.vse.java.checkers.client.Networking.MessageListeners.WaitingRoomListener;
import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.MessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.client.Networking.Message;
import cz.vse.java.checkers.common.ServerMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** * Controller pro obsluhu obrazovky čekárny
 * @author Adam Filinger
 * @version 1.0
 *
 * */
public class WaitingRoomController extends Controller implements WaitingRoomListener {
    private static final Logger log = LoggerFactory.getLogger(WaitingRoomController.class);

    private final MessageHandler handler = Connection.getInstance().getHandler();
    private final MessageEventBus eventBus = MessageEventBus.getInstance();

    private final Set<String> playersAvailable = new HashSet<>();
    private final Set<String> requestedMatches = new HashSet<>();
    private final Set<String> requestingMatches = new HashSet<>();

    @FXML
    private ListView<String> availablePlayersList;

    @FXML
    private ListView<String> playersRequestingList;

    @FXML
    private void initialize() {
        availablePlayersList.setOnMouseClicked(event ->
                requestMatch(availablePlayersList.getSelectionModel().getSelectedItem())
        );
        playersRequestingList.setOnMouseClicked(event ->
                acceptMatch(playersRequestingList.getSelectionModel().getSelectedItem())
        );

        // Registrace jako listener
        eventBus.registerWaitingRoomListener(this);
    }


    // ===== Observer implementace =====


    /**
     * Aktualizace čekajících hráčů při příchozí zprávě serveru
     * Kontrola vyždáných zápasů a vlastního jména
     * @param playersList
     */
    @Override
    public void onPlayersUpdated(String[] playersList) {
        Platform.runLater(() -> {
            playersAvailable.clear();
            playersAvailable.addAll(Arrays.asList(playersList));

            if (availablePlayersList != null) {
                availablePlayersList.getItems().clear();
                for (String name : playersAvailable) {
                    if (!Objects.equals(name, Connection.getInstance().getName())) {
                        if (!requestingMatches.contains(name) && !requestedMatches.contains(name)) {
                            availablePlayersList.getItems().add(name);
                        }
                    }
                }
            }
        });
    }

    /**
     * Aktualizace vyžádaných zápasů při příchozí zprávě serveru
     * včetně odstranění z listu dostupných hráčů a kontroly vlastního jména.
     * Odstranění hráče z listu čekajících zápasů při odchodu hráče z čekárny do hry
     *
     * @param playerName
     * @param unmatched
     */
    @Override
    public void onRequestingMatchesUpdated(String playerName, boolean unmatched) {
        Platform.runLater(() -> {
            if (playersRequestingList != null) {
                if (!Objects.equals(playerName, Connection.getInstance().getName())) {
                    if (unmatched) {
                        requestingMatches.remove(playerName);
                        playersAvailable.remove(playerName);
                        requestedMatches.remove(playerName);
                        availablePlayersList.getItems().remove(playerName);
                        playersRequestingList.getItems().remove(playerName);
                    } else {
                        requestingMatches.add(playerName);
                        playersRequestingList.getItems().add(playerName);
                        availablePlayersList.getItems().remove(playerName);
                    }
                }
            }
        });
    }


    @Override
    public void onSetupMatch(String playerName) {
        cleanLists();
        Platform.runLater(() -> {
            Stage stage = (Stage) availablePlayersList.getScene().getWindow();
            stage.setScene(getNextScene());
        });
    }

    // ===== UI akce =====

    /**
     * Přijetí zápasu - odstraní ze seznamu čekajících zápasů.
     * Update UI
     * @param playerName
     */
    private void acceptMatch(String playerName) {
        String UID = generateID();
        CompletableFuture<Message> responseFuture = rm.registerRequest(UID);
        if (handler.send(ClientMessage.MATCH, UID, playerName)) {
            responseFuture.thenAccept(response -> Platform.runLater(() -> {
                if (Objects.equals(response.getToken(), ServerMessage.OK.name())) {
                    requestingMatches.remove(playerName);
                    requestedMatches.remove(playerName);
                    playersRequestingList.getItems().remove(playerName);

                    log.info("Match accepted from: {}", playerName);
                } else {
                    log.error("Invalid credentials");
                }
            })).orTimeout(5, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> log.error("Network timeout on match acceptance"));
                        return null;
                    });
        }
    }

    private void requestMatch(String playerName) {
        String UID = generateID();
        if (handler.send(ClientMessage.MATCH, UID, playerName)) {
            log.info("Sent match request to: {}", playerName);
            requestedMatches.add(playerName);
            availablePlayersList.getItems().remove(playerName);
        } else {
            if (playersAvailable.contains(playerName)) {
                log.info("Player {} exists but request match failed to send", playerName);
            } else {
                log.info("Player {} doesn't exist", playerName);
            }
        }
    }

    public void cleanLists(){
        requestedMatches.clear();
        requestingMatches.clear();
        playersRequestingList.getItems().clear();
        playersRequestingList.getItems().clear();
    }



}
