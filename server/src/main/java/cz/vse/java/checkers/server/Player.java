package cz.vse.java.checkers.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a connected player on the server.
 *
 * Stores player state including name, active match, matchmaking requests,
 * rematch requests, and score history against opponents.
 *
 * Each Player instance is bound to a ClientHandler and represents
 * a single connected client.
 *
 * @author Jan Hrdonka, Adam Filinger
 * @version 1.0
 */
public class Player {
    private final ClientHandler clientHandler;
    private final Set<Player> offeredMatches;
    private final Set<Player> offeredRematches;
    private Match match;
    private String name;
    private boolean isInWaitingRoom = false;

    private final Map<String, Integer> opponentScores;

    public Player(ClientHandler clientHandler){
        this.clientHandler = clientHandler;
        offeredMatches = new HashSet<>();
        offeredRematches = new HashSet<>();
        name = "";
        opponentScores = new java.util.HashMap<>();
    }

    /**
     * Returns the score of this player against a given opponent.
     * If no previous match exists, returns 0.
     */
    public synchronized int getScoreAgainst(Player opponent) {
        if (opponent == null) return 0;
        String oppName = opponent.getName();
        return opponentScores.getOrDefault(oppName, 0);
    }

    /**
     * Increments the score of this player against a given opponent.
     */
    public synchronized void incrementScoreAgainst(Player opponent) {
        if (opponent == null) return;
        String oppName = opponent.getName();
        int old = opponentScores.getOrDefault(oppName, 0);
        opponentScores.put(oppName, old + 1);
    }

    /**
     * Sets the score against a specific opponent (used for reset or override).
     */
    public synchronized void setScoreAgainst(Player opponent, int value) {
        if (opponent == null) return;
        opponentScores.put(opponent.getName(), value);
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    /**
     * Registers a match offer from another player.
     */
    public synchronized void offerMatch(Player player){
        offeredMatches.add(player);
    }

    public synchronized void removeMatch(Player player){
        offeredMatches.remove(player);
    }

    public synchronized ClientHandler getClientHandler(){
        return clientHandler;
    }

    /**
     * Checks whether this player has received a match offer from the given player.
     */
    public synchronized boolean wantsMatch(Player player){
        return offeredMatches.contains(player);
    }

    public synchronized void setMatch(Match match){
        this.match = match;
    }

    public synchronized void clearOfferedMatches(){
        offeredMatches.clear();
    }

    public synchronized Set<Player> getOfferedMatches(){
        return offeredMatches;
    }

    public synchronized Match getMatch(){
        return match;
    }

    public synchronized boolean isInWaitingRoom() {
        return isInWaitingRoom;
    }

    public synchronized void setInWaitingRoom(boolean inWaitingRoom) {
        isInWaitingRoom = inWaitingRoom;
    }

    /**
     * Checks whether this player has offered a rematch to the given opponent.
     */
    public synchronized boolean wantsRematch(Player player){
        return offeredRematches.contains(player);
    }

    public synchronized void clearOfferedRematches(){
        offeredRematches.clear();
    }
}