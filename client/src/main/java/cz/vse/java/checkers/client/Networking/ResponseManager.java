package cz.vse.java.checkers.client.Networking;

import cz.vse.java.checkers.common.Message;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ResponseManager {
    // Stores pending responses mapped by their unique Correlation ID
    private final Map<String, CompletableFuture<Message>> pendingRequests = new ConcurrentHashMap<>();

    public static ResponseManager instance;

    private ResponseManager() {}

    public static ResponseManager getInstance() {
        if (instance == null){
            synchronized (ResponseManager.class){
                if (instance == null){
                    instance = new ResponseManager();
                }
            }
        }
        return instance;
    }

    // Called by the Controller before sending a request
    public CompletableFuture<Message> registerRequest(String ID) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        pendingRequests.put(ID, future);
        return future;
    }

    // Called by the MessageHandler when a response arrives
    public void dispatchResponse(String correlationId, Message response) {
        CompletableFuture<Message> future = pendingRequests.remove(correlationId);
        if (future != null) {
            future.complete(response); // This wakes up/notifies the waiting Controller
        }
    }

}
