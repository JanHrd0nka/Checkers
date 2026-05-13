package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

import static cz.vse.java.checkers.common.ServerMessage.OK;

public class ClientHandler implements Runnable {

    private final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final Server server;

    private PrintWriter out;
    private BufferedReader in;

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
        if (validateLenght(2, tokens))
        {
            var wr = server.getWaitingRoom();
            if (wr.addPlayer(tokens[1])) {
                send(ServerMessage.OK);
                try
                {
                    Thread.sleep(3000);
                } catch (InterruptedException e){

                }
                send(ServerMessage.OK, "message");
            }
            else {
                send(ServerMessage.ERROR, "Jmeno obsazeno");
            }
        }
    }

    private void handleMatch(String[] tokens) {
        log.info("MATCH request");
    }

    private void handleSetup(String[] tokens) {
        log.info("SETUP request");
    }

    private void handleMove(String[] tokens) {
        log.info("MOVE request");
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

    public void send(ServerMessage name, String content) {
        String message = name.name() + " " + content;
        out.println(message);
    }

    public void send(ServerMessage name){
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
}