package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Pos;
import cz.vse.java.checkers.common.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClientHandler implements Runnable {

    private final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final Server server;

    private PrintWriter out;
    private BufferedReader in;
    private Player player;

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
                case DRAW -> handleDraw(tokens);
                case SURRENDER -> handleSurrender(tokens);
                case QUIT -> disconnect();
                case JOIN_WAITING_ROOM -> handleJoinWaitingRoom(tokens);
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
                    opponent.offerMatch(player);
                    setUpBeforeGame(player, playerName, opponentName, opponent);
                    setUpBeforeGame(opponent, opponentName, playerName, player);
                    send(ServerMessage.OK, tokens[1]);
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
        Set<Player> playersToUnmatch = player.getOfferedMatches();
        for (Player playerToUnmatch : playersToUnmatch){
            //if (playerToUnmatch != opponent)
            //{
                ClientHandler handler = playerToUnmatch.getClientHandler();
                handler.send(ServerMessage.UNMATCH, "server-id " + playerName);
            //}
        }
        player.clearOfferedMatches();
        player.clearOfferedRematches();
        player.setName(server.getWaitingRoom().getName(player));
        server.getWaitingRoom().setPlayerInWaitingRoom(player, false);
        ClientHandler handler = player.getClientHandler();
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
            if (time > 0)
            {
                var match = player.getMatch();
                Player opponent = match.getOpponent(player);
                Player white;
                if (isW) {
                    white = player;
                } else {
                    white = opponent;
                }
                match.setUp(time, white, isMust, server.getTimeSender());
                send(ServerMessage.OK, tokens[1]);
                send(ServerMessage.SETUP_DONE, "server-id " + time + " " + isW + " " + isMust);
                opponent.getClientHandler().send(ServerMessage.SETUP_DONE, "server-id " + time + " " + !isW + " " + isMust);
            }
        }
    }

    private void handleMove(String[] tokens) {
        log.info("MOVE request");
        if (validateGameAndLength(3, tokens)){
            String move = tokens[2];
            boolean isSyntaxValid =
                    move.matches("[0-7]+")
                            && move.length() >= 4
                            && move.length() % 2 == 0;
            if (isSyntaxValid){
                List<Pos> path = new ArrayList<>();
                for (int i = 0; i < move.length(); i += 2) {
                    int x = move.charAt(i) - '0';
                    int y = move.charAt(i + 1) - '0';
                    path.add(new Pos(x, y));
                }
                Match match = player.getMatch();
                String error = match.makeMove(player, path);
                if (error.isEmpty()){
                    String currentState = match.getGameContent();
                    send(ServerMessage.OK, tokens[1]);
                    send(ServerMessage.STATE, "server-id " + currentState);
                    match.getOpponent (player).getClientHandler().send(ServerMessage.STATE, "server-id " + currentState);
                    if (!match.checkGameState()){
                        sendResult(player, false);
                        server.getTimeSender().remove(match.getGame());
                    }
                }
                else{
                    log.warn("Invalid move");
                    send (ServerMessage.ERROR, tokens[1] + error);
                }
            }
            else{
                log.warn("Invalid move syntax");
                send (ServerMessage.ERROR, tokens[1] + " Invalid_move_syntax");
            }
        }
    }

    private void handleHistory(String[] tokens) {
        log.info("HISTORY request");
        if (validateGameAndLength(3, tokens)){
            int index;
            try {
                index = Integer.parseInt(tokens[2]);
                String history = player.getMatch().getHistory(index);
                if (history.isEmpty()){
                    send(ServerMessage.ERROR, tokens[1] + " Histrory_index_out_of_bounds.");
                }
                else{
                    send(ServerMessage.HISTORY, tokens[1] + " " + history);
                }
            }
            catch (NumberFormatException e){
                send (ServerMessage.ERROR, tokens[1] + " Invalid_history_index");
            }
        }
    }

    private void handleDraw(String[] tokens) {
        log.info("DRAW request");
        Match match = player.getMatch();
        Player opponent = match.getOpponent(player);
        if(validateLenght(2, tokens)){
            drawResult(tokens, match, opponent);
        }else if(validateLenght(3, tokens)){
            boolean accepted = tokens[2].equals("accept");
            if(accepted){
                drawResult(tokens, match, opponent);
            }else{
                match.setDrawOffered(false);
                opponent.getClientHandler().send(ServerMessage.DRAW, "server-id no");
            }
            send(ServerMessage.OK, tokens[1]);

        }

    }

    private void drawResult(String[] tokens, Match match, Player opponent) {
        if (match.isDrawOffered()){
            send(ServerMessage.OK, tokens[1]);
            int playerScore = player.getScoreAgainst(opponent);
            int opponentScore = opponent.getScoreAgainst(player);
            String score = playerScore + ":" + opponentScore;
            send(ServerMessage.RESULT, tokens[1] + " draw " + score);
            opponent.getClientHandler().send(ServerMessage.RESULT, tokens[1] + " draw " + score);
        }
        else{
            match.setDrawOffered(true);
            opponent.getClientHandler().send(ServerMessage.DRAW, "server-id offered");
            send(ServerMessage.OK, tokens[1]);
        }
    }

    private void handleSurrender(String[] tokens) {
        log.info("SURRENDER request");
        if(validateLenght(2, tokens)){
            sendResult(player, true);
            log.info("Sent surrender results");
        }
    }

    private void handleJoinWaitingRoom(String[] tokens) {
        log.info("JOIN WAITING ROOM");
        //server.addToWaitingRoom(this);
        if (validateLenght(3, tokens))
        {
            var wr = server.getWaitingRoom();
            boolean success;
            // Hráč se vrací do waiting room - musíme kontrolovat, že jméno není obsazeno JINÝM hráčem v waiting room
            Player existingPlayer = wr.getPlayer(tokens[2]);
            if (existingPlayer != null && existingPlayer != player) {
                success = false;
            } else {
                wr.renamePlayer(player, tokens[2]);
                wr.setPlayerInWaitingRoom(player, true);  // Nastavit stav
                success = true;
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

    private void handleReplay(String[] tokens) {
        log.info("REPLAY (rematch) request");
        if (validatePlayerAndLength(3, tokens)) {
            var wr = server.getWaitingRoom();
            Player opponent = player.getMatch().getOpponent(player);
            if(tokens[2].equals("no")){
                wr.setPlayerInWaitingRoom(player, true);
                send(ServerMessage.OK, tokens[1] + " to-WR");
//                opponent.getClientHandler().send(ServerMessage.REMATCH, tokens[1] + " no");
//                wr.setPlayerInWaitingRoom(opponent, true);
                server.broadcast(ServerMessage.PLAYERS_WAITING, wr.getPlayerNames());
            } else if (opponent != null) {
                // Pokud už opponent nabídl rematch tomuto hráči -> oboustranný souhlas
                if (opponent.wantsRematch(player)) {
                    String playerName = wr.getName(player);
                    String opponentName = wr.getName(opponent);

                    // Pokud už existuje Match instance mezi nimi, použijeme ji (tak zachováme skóre).
                    // Pokud žádná instance neexistuje nebo reference neodpovídá, vytvoříme novou.
                    Match currentMatch = player.getMatch();
                    if (currentMatch == null || currentMatch.getOpponent(player) != opponent) {
                        // vytvoříme novou instanci Match (to je fallback)
                        new Match(player, opponent);
                    }

                    // Oba jdou do SETUP, použijeme stávající setUpBeforeGame (který už neodstraňuje jména)
//                    setUpBeforeGame(player, playerName, opponentName, opponent);
//                    setUpBeforeGame(opponent, opponentName, playerName, player);

                    send(ServerMessage.REMATCH, "accepted");
                    opponent.getClientHandler().send(ServerMessage.REMATCH, "accepted");


                    // Notifikujeme waiting-room klienty (pokud je třeba)
                    server.broadcast(ServerMessage.PLAYERS_WAITING, wr.getPlayerNames());
                }
                else {
                    // Uložíme rematch nabídku a notify opponent
                    player.offerMatch(opponent);
                    var oppClient = opponent.getClientHandler();
                    oppClient.send(ServerMessage.MATCH, "server-id " + wr.getName(player));
                    send(ServerMessage.OK, tokens[1] + " to-WR");
                }
            } else {
                send(ServerMessage.ERROR, tokens[1] + " Hrac_neni_dostupny");
            }
        }
    }

    public synchronized void sendResult(Player player, boolean isSurrender){
        Match match = player.getMatch();
        Player winner;
        Player loser;
        String ID = isSurrender ? "surrender" : "server-id";
        if(isSurrender){
            winner = match.getOpponent(player);
            loser = player;
        }else{
            winner = match.getWinner();
            loser = match.getOpponent(winner);
        }
        winner.incrementScoreAgainst(loser);

        int winnerScore = winner.getScoreAgainst(loser);
        int loserScore = loser.getScoreAgainst(winner);
        String score = winnerScore + ":" + loserScore;

        winner.getClientHandler().send(ServerMessage.RESULT, ID + " " + winner.getName() + " " + score);
        loser.getClientHandler().send(ServerMessage.RESULT, ID + " " + winner.getName()+ " " + score);
        match.endGame();
    }



    public synchronized void send(ServerMessage name, String content) {
        String message = name.name() + " " + content;
        log.info("Sending message: {}", message);
        out.println(message);
    }

    private void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Error closing socket", e);
        }

       server.removeClient(this);
        WaitingRoom wr = server.getWaitingRoom();
        wr.disconnectPlayer(player);

        server.broadcast(ServerMessage.PLAYERS_WAITING, wr.getPlayerNames());
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

    private boolean validateGameAndLength(int expectedLenght, String[] tokens){
        boolean result = validatePlayerAndLength(expectedLenght, tokens);
        if (result){
            var match = player.getMatch();
            if (match != null){
                if (!match.isSetup()){
                    log.warn("Match is not setup yet");
                    send(ServerMessage.ERROR, "Zapas jeste neni nastaven");
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