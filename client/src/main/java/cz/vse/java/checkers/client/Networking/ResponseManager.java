package cz.vse.java.checkers.client.Networking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/** Třída zajišťující správné párování odeslaných a příchozích zpráv
 * @author Adam Filinger
 * @version 1.0
 */
public class ResponseManager {
    // Stores pending responses mapped by their unique Correlation ID
    private final Map<String, CompletableFuture<Message>> pendingRequests = new ConcurrentHashMap<>();

    public static volatile ResponseManager instance;

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

    /**
     * Uložení zprávy před zasláním na server - nutno volat z controlleru kde posílám zprávu
     * @param ID
     * @return
     */
    public CompletableFuture<Message> registerRequest(String ID) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        pendingRequests.put(ID, future);
        return future;
    }

    /**
     * Odbavení příchozí zprávy dle ID a notifikace čekajícího controlleru
     * @param correlationId
     * @param response
     */
    public void dispatchResponse(String correlationId, Message response) {
        CompletableFuture<Message> future = pendingRequests.remove(correlationId);
        if (future != null) {
            future.complete(response); // This wakes up/notifies the waiting Controller
        }
    }

}
