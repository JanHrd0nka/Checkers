package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Main server component responsible for accepting client
 * connections, managing connected clients, and coordinating
 * communication between them.
 *
 * The server maintains a waiting room for matchmaking and
 * provides broadcast functionality for server-wide messages.
 *
 * @author Jan Hrdonka
 * @version 1.0
 */
public class Server {
    private final Logger log;
    private final int port;
    private final Set<ClientHandler> clients = new HashSet<>();
    private final WaitingRoom waitingRoom;
    private final TimeSender timeSender;

    public Server(int port) {
        this.port = port;
        log = LoggerFactory.getLogger(Server.class);
        waitingRoom = new WaitingRoom();
        timeSender = new TimeSender();
    }

    public void start() {
        log.info("Starting server on port {}...", port);
        Thread serverThread = new Thread(this::run);
        serverThread.start();
    }

    public synchronized void broadcast(ServerMessage name, String message){
        log.info("----Broadcast Started----");
        for (ClientHandler client : clients){
            client.send(name, "server-id " + message);
        }
        log.info("----Broadcast finished---");
    }

    public synchronized WaitingRoom getWaitingRoom(){
        return waitingRoom;
    }

    private void run() {
        log.info("Server Thread started");
        log.info("Listening on port {}", port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                log.info("Client connected: {}", clientSocket);
                new Thread(client).start();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            log.info("Server stopped");
        }
    }

    public TimeSender getTimeSender(){
        return timeSender;
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}