package cz.vse.java.checkers.server;

import java.util.HashSet;
import java.util.Set;

public class Player {
    private final ClientHandler clientHandler;
    private Set<Player> offeredMatches;
    private Match match;

    public Player (ClientHandler clientHandler){
        this.clientHandler = clientHandler;
        offeredMatches = new HashSet<>();
    }

    public synchronized void offerMatch(Player player){
        offeredMatches.add(player);
    }
    public synchronized void removeMatch(Player player){
        offeredMatches.remove(player);
    }

    public synchronized ClientHandler getClientHandler(){
        return clientHandler;
    }

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
}
