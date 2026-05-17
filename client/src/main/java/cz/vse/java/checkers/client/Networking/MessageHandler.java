package cz.vse.java.checkers.client.Networking;

import cz.vse.java.checkers.common.Message;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MessageHandler {
    public void onConnect() {}
    public void onConnectionFailed(IOException e) {}
    public void onMessage(String message) {}
    public void onDisconnect() {}
    public CompletableFuture<Message> registerRequest(String correlationId) {
        return null;
    }
}
