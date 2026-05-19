package cz.vse.java.checkers.client.Networking;

import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Message;
import cz.vse.java.checkers.common.ServerMessage;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class Connection {

    private static Connection instance;

    private final Logger log = LoggerFactory.getLogger(Connection.class);
    Set<MessageHandler> messageHandlers = new CopyOnWriteArraySet<>();
    private final BlockingQueue<String> sendQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> receiveQueue = new LinkedBlockingQueue<>();
    private final String stopMessage = "__QUIT__";
    private Thread writerThread;
    private Thread readerThread;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private SampleMessageHandler messageHandler;


    private String name;

    private Connection(){
        System.out.println("Connection created");
    }

    public static Connection getInstance(){
        if (instance == null){
            synchronized (Connection.class){
                if (instance == null){
                    instance = new Connection();
                }
            }
        }
        return instance;
    }

    public void setName(String setName){
        name = setName;
    }
    public String getName(){
        return name;
    }




    public void addMessageHandler(SampleMessageHandler handler){
        messageHandlers.add(handler);
        messageHandler = handler;
    }
    public void removeMessageHandler(MessageHandler handler){
        messageHandlers.remove(handler);
    }

    public SampleMessageHandler getHandler(){
        return messageHandler;
    }


    //----- sendQueue methods

    public boolean send(ClientMessage name, String content) {
        String message = name.name() + " " + content;

        boolean result = sendQueue.offer(message);
        if (!result) {
            log.error("Send queue full!");
        }
        return result;
    }
    public void connect(String host, int port) {
        readerThread = new Thread(() -> {
            log.info("Thread started");
            try {
                socket = new Socket(host, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                log.info("Connected!");
                startWriter();
                runOnFxThread(MessageHandler::onConnect);
                startReader();
            } catch (IOException e) {
                log.error("Connection failed", e);
                runOnFxThread(h -> h.onConnectionFailed(e));
            }
        });
        readerThread.setName("Socket-Reader");
        readerThread.start();
    }
    public void disconnect(){
        log.info("Disconnecting...");
        try {
            // stop writer
            if (!sendQueue.offer(stopMessage)){
                throw new RuntimeException("Send queue is full");
            }

            // stop reader
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            log.info("Disconnected");

        } catch (IOException e) {
            log.error("Disconnect error: ", e);
        }
    }
    private void startWriter() {
        writerThread = new Thread(() -> {
            log.info("Thread started");
            try {
                while (!socket.isClosed()) {
                    String msg = sendQueue.take();
                    if (msg.equals(stopMessage)){
                        log.info("Thread interrupted");
                        break;
                    }
                    out.println(msg);
                    log.info("Sent: {}", msg);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to send: {}", e.getMessage());
            }
            log.info("Thread stopped");
        });
        writerThread.setName("Socket-Writer");
        writerThread.start();
    }

    private void startReader(){
        try {
            String line;
            while ((line = in.readLine()) != null) {
                String message = line;
                log.info("Received: {}", message);
                runOnFxThread(h -> h.onMessage(message));
            }
        } catch (IOException e) {
            log.error("Reader error", e);
            runOnFxThread(MessageHandler::onDisconnect);
        }
        log.info("Thread stopped");
    }
    private void runOnFxThread(java.util.function.Consumer<MessageHandler> action) {
        Platform.runLater(() -> {
            for (var handler : messageHandlers) {
                action.accept(handler);
            }
        });
    }


}