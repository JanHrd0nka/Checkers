package cz.vse.java.checkers.common;

/**
 * Represents messages sent from the server to the client.
 * <p>
 * Each enum value corresponds to a specific response type in the
 * communication protocol. Messages are transmitted over the network
 * as strings and then converted back and forth between enum values
 * using {@code name()} (serialization) and {@code valueOf()} (deserialization).
 * <p>
 * This enum defines all possible server responses, including game state
 * updates, matchmaking events, errors, and results.
 *
 * @author Jan Hrdonka
 * @version 1.0
 */
public enum ServerMessage {
    OK,
    ERROR,
    PLAYERS_WAITING,
    MATCH,
    UNMATCH,
    SETUP,
    SETUP_DONE,
    STATE,
    MOVED,
    HISTORY,
    RESULT,
    REMATCH,
    DRAW,
    TIME
}
