package cz.vse.java.checkers.client.Networking;

import java.util.*;

/**
 * Represents a parsed network message received from the server.
 *
 * Incoming messages are expected in the format:
 * {@code TOKEN ID CONTENT...}
 *
 * This class splits the raw string into:
 * - token: message type
 * - id: message identifier
 * - content: remaining message payload
 *
 * NOTE: Content is currently stored as a string representation
 * of the remaining parts of the message.
 *
 *
 * @author Adam Filinger
 * @version 1.0
 */
public class Message {
    private final String ID;
    private final String token;
    private final String content;

    public Message(String message){
        ArrayList<String> parts = new ArrayList<>(List.of(message.split(" ")));
        token = parts.getFirst();
        parts.removeFirst();
        ID = parts.getFirst();
        parts.removeFirst();
        content = parts.toString();
    }

    public String getToken() {
        return token;
    }

    public String getContent() {
        return content;
    }

    public String getID(){
        return ID;
    }

}
