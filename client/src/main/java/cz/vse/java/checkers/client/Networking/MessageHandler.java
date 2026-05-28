package cz.vse.java.checkers.client.Networking;


import cz.vse.java.checkers.client.Game.GameController;
import cz.vse.java.checkers.common.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** * MessageHandler pouze pasuje zprávy ze serveru a publikuje eventy. * NEMÁ ŽÁDNÉ REFERENCE NA UI KOMPONENTY. */
public class MessageHandler {
    private final Connection connection;
    private final MessageEventBus eventBus;
    private final ResponseManager rm;
    private final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    public MessageHandler(Connection connection, ResponseManager rm, MessageEventBus eventBus) {
        this.connection = connection;
        this.rm = rm;
        this.eventBus = eventBus;
    }

    public void onConnect() {
        logger.info("Connected to server");
    }

    public boolean send(ClientMessage clientMessage, String UID, String content) {
        return connection.send(clientMessage, UID + " " + content);
    }

    public void onMessage(String message) {
        logger.info("Received message: {}", message);

        try {
            Message msg = new Message(message);
            String ID = msg.getID();
            String contentStr = msg.getContent();

            // Bezpečné parsování obsahu (kezde null-safe)
            String[] content = parseContent(contentStr);

            if (content.length > 0) {
                ServerMessage msgType = ServerMessage.valueOf(msg.getToken());
                publishEvent(msgType, content, ID);
            }

            rm.dispatchResponse(ID, msg);
        } catch (Exception e) {
            logger.error("Error processing message: {}", message, e);
        }
    }

    /**     * Bezpečné parsování obsahu ze zprávy     */
    private String[] parseContent(String contentStr) {
        try {
            if (contentStr == null || contentStr.isEmpty()) {
                return new String[0];
            }
            String extracted = StringUtils.substringBetween(contentStr, "[", "]");
            if (extracted == null || extracted.isEmpty()) {
                return new String[0];
            }
            return extracted.split(", ");
        } catch (Exception e) {
            logger.warn("Failed to parse message content: {}", contentStr, e);
            return new String[0];
        }
    }

    /**     * Publikuje event na základě typu serveru zprávy     */
    private void publishEvent(ServerMessage msgType, String[] content, String ID) {
        switch (msgType) {
            case PLAYERS_WAITING -> eventBus.publishPlayersUpdated(content);
            case MATCH -> {
                if (content.length > 0) {
                    eventBus.publishRequestingMatchesUpdated(content[0], false);
                }
            }
            case SETUP -> {
                if (content.length > 0) {
                    eventBus.publishSetupMatch(content[0]);
                }
            }
            case UNMATCH -> {
                if (content.length > 0) {
                    eventBus.publishRequestingMatchesUpdated(content[0], true);
                }
            }
            case STATE -> handleStateEvent(content, ID);
            case RESULT -> handleGameResult(content, ID);
            case REMATCH -> handleRematchEvent(content);
            case DRAW -> {
                if(content[0].equals("offered")) {
                    eventBus.publishDrawOffer();
                }
            }
            default -> logger.warn("Unknown server message type: {}", msgType);
        }
    }

    private void handleStateEvent(String[] content, String ID) {
        if (content.length == 3 && ID.equalsIgnoreCase("match-setup")) {
            try {
                int time = Integer.parseInt(content[0]);
                boolean isWhite = content[1].equalsIgnoreCase("true");
                boolean mustTake = content[2].equalsIgnoreCase("true");
                eventBus.publishSetupReceived(time, isWhite, mustTake);
            } catch (NumberFormatException e) {
                logger.error("Failed to parse setup state", e);
            }
        } else if (ID.equalsIgnoreCase("op-moved")) {
            if (content.length > 0) {
                eventBus.publishOpponentMoved(content[0]);
            }
        }
    }

    private void handleGameResult(String[] content, String ID) {
        if (content.length >= 2) {
            String winner = content[0];
            String score = content[1];
            eventBus.publishGameResult(winner, score);
        }
    }

    private void handleRematchEvent(String[] content) {
        if (content.length > 0) {
            String rematchStatus = content[0].toLowerCase();

            switch (rematchStatus) {
                case "accepted" -> {
                    logger.info("Rematch accepted");
                    eventBus.publishRematchOffer(true);
                }
                case "no" -> {
                    logger.info("Rematch declined");
                    eventBus.publishRematchOffer(false);
                }
                default -> logger.warn("Unknown rematch status: {}", rematchStatus);
            }
        }
    }

    public void onConnectionFailed(IOException e) {
        logger.error("Connection failed: {}", e.getMessage());
    }

    public void onDisconnect() {
        logger.info("Disconnected from server");
    }

}
