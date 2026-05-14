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

    public void offerMatch(Player player){
        offeredMatches.add(player);
    }
    public void removeMatch(Player player){
        offeredMatches.remove(player);
    }

    public ClientHandler getClientHandler(){
        return clientHandler;
    }

    public boolean wantsMatch(Player player){
        return offeredMatches.contains(player);
    }

    public void setMatch(Match match){
        this.match = match;
    }
    void clearOfferedMatches(){
        offeredMatches.clear();
    }
    Set<Player> getOfferedMatches(){
        return offeredMatches;
    }
}
