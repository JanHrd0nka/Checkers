package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.OptionPaneUI;
import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.random.RandomGenerator;

import static cz.vse.java.checkers.common.ServerMessage.OK;

public class ClientHandler implements Runnable {

    private final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final Server server;

    private PrintWriter out;
    private BufferedReader in;
    private Player player;

    // TODO: private GameSession session;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        log.info("Client created: {}", socket);
    }

    @Override
    public void run() {
        try {
            setupStreams();

            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void setupStreams() throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void handleMessage(String message) {
        log.debug("Received: {}", message);

        String[] tokens = message.split(" ");

        if (tokens.length > 0)
        {
            ClientMessage type;

            try {
                type = ClientMessage.valueOf(tokens[0]);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown message: {}", tokens[0]);
                return;
            }
            switch (type) {
                case LOGIN -> handleLogin(tokens);
                case MATCH -> handleMatch(tokens);
                case UNMATCH -> handleUnmatch(tokens);
                case SETUP -> handleSetup(tokens);
                case MOVE -> handleMove(tokens);
                case HISTORY -> handleHistory(tokens);
                case DRAW -> handleDraw();
                case SURRENDER -> handleSurrender();
                case QUIT -> disconnect();
                case JOIN_WAITING_ROOM -> handleJoinWaitingRoom();
                case REPLAY -> handleReplay(tokens);

                default -> log.warn("Unhandled message: {}", type);
            }
        }
    }
    // --- handlers ---

    private void handleLogin(String[] tokens) {
        log.info("LOGIN request");
        if (validateLenght(3, tokens))
        {
            var wr = server.getWaitingRoom();
            boolean success = true;
            if (player == null){
                player = wr.addPlayer(tokens[2], this);
                if (player == null) {
                    success = false;
                }
            }
            else{
                success = wr.renamePlayer(player, tokens[2]);
            }
            if (success){
                send(ServerMessage.OK, tokens[1]);
                server.broadcast(ServerMessage.PLAYERS_WAITING, wr.getPlayerNames());
            }
            else{
                send(ServerMessage.ERROR, tokens[1] + " Jmeno_obsazeno");
            }
        }
    }
    private void handleMatch(String[] tokens) {
        log.info("MATCH request");
        if (validatePlayerAndLength(3, tokens)){
            var wr = server.getWaitingRoom();
            Player opponent = wr.getPlayer(tokens[2]);
            if (opponent != null){
                if (opponent.wantsMatch(player)){
                    String playerName = wr.getName(player);
                    String opponentName = wr.getName(opponent);
                    setUpBeforeGame(player, playerName, opponentName, opponent);
                    setUpBeforeGame(opponent, opponentName, playerName, player);
                    new Match(player, opponent);
                    server.broadcast(ServerMessage.PLAYERS_WAITING,wr.getPlayerNames());
                } else {
                    player.offerMatch(opponent);
                    var oppClient = opponent.getClientHandler();
                    oppClient.send(ServerMessage.MATCH, "server-id " + wr.getName(player));
                    send(ServerMessage.OK, tokens[1]);
                }
            }
            else {
                send(ServerMessage.ERROR, tokens[1] + " Hrac_neni_dostupny");
            }
        }
    }
    private void setUpBeforeGame(Player player, String playerName, String opponentName, Player opponent){
        var playersToUnmatch = player.getOfferedMatches();
        for (var playerToUnmatch : playersToUnmatch){
            if (playerToUnmatch != opponent)
            {
                var handler = playerToUnmatch.getClientHandler();
                handler.send(ServerMessage.UNMATCH, "server-id " + playerName);
            }
        }
        player.clearOfferedMatches();
        server.getWaitingRoom().removePlayer(player);
        var handler = player.getClientHandler();
        handler.send(ServerMessage.SETUP, "server-id " + opponentName);
    }
    private void handleUnmatch(String[] tokens) {
        log.info("UNMATCH request");
        if (validatePlayerAndLength(3, tokens)){
            var wr = server.getWaitingRoom();
            var opponent = wr.getPlayer(tokens[2]);
            if (opponent != null){
                player.removeMatch(opponent);
                opponent.removeMatch(player);
                var client = opponent.getClientHandler();
                client.send(ServerMessage.UNMATCH, "server-id " + wr.getName(player));
                send(ServerMessage.OK, tokens[1]);
            }
            else{
                send(ServerMessage.ERROR, tokens[1] + " Hrac_neni_dostupny");
            }
        }
    }

    private void handleSetup(String[] tokens) {
        log.info("SETUP request");
        if (validateSetupAndLength(5, tokens)){
            boolean isW = "w".equalsIgnoreCase(tokens[3]);
            boolean isMust = "MUST".equalsIgnoreCase(tokens[4]);
            int time = -1;
            try {
                time = Integer.parseInt(tokens[2]);
                if (time > 0) {
                    log.info("Parsed setup: value={}, isW={}, isMust={}", time, isW, isMust);
                }
                else {
                    throw new NumberFormatException("Negative time");
                }
            }
            catch (NumberFormatException e) {
                log.warn("Invalid integer in SETUP: {}", tokens[2]);
                send (ServerMessage.ERROR, tokens[1] + " Invalid_setup");
            }
            var match = player.getMatch();
            Player opponent = player.getMatch().getOpponent(player);
            Player white;
            if (isW){
                white = player;
            }
            else{
                white = opponent;
            }
            match.setUp(time, white, isMust);
            send(ServerMessage.OK, tokens[1]);
            send(ServerMessage.STATE, tokens[1]);
            opponent.getClientHandler().send(ServerMessage.STATE, "server-id");
        }
    }

    private void handleMove(String[] tokens) {
        log.info("MOVE request with ID: {}", tokens[1]);
        if(RandomGenerator.getDefault().nextBoolean()){
            send(ServerMessage.OK, tokens[1]);
            //docasny zasilani tahu protivnikovy, pouze pro test
            player.getMatch().getOpponent(player).getClientHandler().send(ServerMessage.MOVED,"server-id " +  tokens[2] + " "  + tokens[3]);
        }else{
            send(ServerMessage.ERROR, tokens[1]);
        }

        // později: server.getGameSession(...).move(...)
    }

    private void handleHistory(String[] tokens) {
        log.info("HISTORY request");
    }

    private void handleDraw() {
        log.info("DRAW request");
    }

    private void handleSurrender() {
        log.info("SURRENDER request");
    }

    private void handleJoinWaitingRoom() {
        log.info("JOIN WAITING ROOM");
        //server.addToWaitingRoom(this);
    }

    private void handleReplay(String[] tokens) {
        log.info("REPLAY request");
    }

    public synchronized void send(ServerMessage name, String content) {
        String message = name.name() + " " + content;
        log.info("Sending message: {}", message);
        out.println(message);
    }

    public synchronized void send(ServerMessage name){
        log.info("Sending message: {}", name.name());
        out.println(name.name());
    }

    private void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Error closing socket", e);
        }

       //server.removeClient(this);
        log.info("Client disconnected");
    }

    private boolean validateLenght(int expectedLenght, String[] tokens){
        boolean result = tokens.length == expectedLenght;
        if (!result){
            log.warn("Invalid message length");
            send(ServerMessage.ERROR, "Invalid message length.");
        }
        return result;
    }

    private boolean validatePlayerAndLength(int expectedLenght, String[] tokens){
        boolean result = player != null;
        if (!result){
            log.warn("Player not registered");
            send(ServerMessage.ERROR, "Nejsi zaregistrovan");
        }
        else{
            result = validateLenght(expectedLenght, tokens);
        }
        return result;
    }

    private boolean validateSetupAndLength(int expectedLenght, String[] tokens){
        boolean result = validatePlayerAndLength(expectedLenght, tokens);
        if (result){
            var match = player.getMatch();
            if (match != null){
                if (match.isSetup()){
                    log.warn("Match already setup");
                    send(ServerMessage.ERROR, "Zapas uz je nastaven");
                }
            }
            else{
                log.warn("Player not paired");
                send(ServerMessage.ERROR, "Nemas soupere");
            }
        }
        return result;
    }
}