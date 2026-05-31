package cz.vse.java.checkers.common;
/**
 * Represents messages sent from the client to the server.
 *
 * Each enum value corresponds to a specific request type in the
 * communication protocol. Messages are transmitted over the network
 * as strings and then converted to this enum on the server side using
 * {@code valueOf()}; responses are similarly converted back to strings
 * when sent to clients.
 *
 * Enum values are serialized as strings and deserialized back
 * using {@code name()} and {@code valueOf()}.
 *
 * @author Jan Hrdonka
 * @version 1.0
 */
public enum ClientMessage {
    LOGIN,
    MATCH,
    UNMATCH,
    SETUP,
    MOVE,
    HISTORY,
    DRAW,
    SURRENDER,
    QUIT,
    JOIN_WAITING_ROOM,
    REPLAY,
}
