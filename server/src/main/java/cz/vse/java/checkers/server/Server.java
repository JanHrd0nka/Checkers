package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private final Logger log;

    private final int port;
    private ServerSocket serverSocket;
    private final Set<ClientHandler> clients = new HashSet<>();
    private WaitingRoom waitingRoom;

    public Server(int port) {
        this.port = port;
        log = LoggerFactory.getLogger(Server.class);
        waitingRoom = new WaitingRoom();
    }

    public void start() {
        log.info("Starting server on port {}...", port);
        Thread serverThread = new Thread(this::run);
        serverThread.start();
    }

    public synchronized void broadcast(ServerMessage name, String message){
        log.info("----Broadcast Started----");
        for (var client : clients){
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
        try {
            serverSocket = new ServerSocket(port);
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
}