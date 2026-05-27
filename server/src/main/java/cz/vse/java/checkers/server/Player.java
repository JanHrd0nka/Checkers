package cz.vse.java.checkers.server;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Player {
    private final ClientHandler clientHandler;
    private Set<Player> offeredMatches;
    private Set<Player> offeredRematches;
    private Match match;
    private String name;
    private boolean isInWaitingRoom = false;

    //private int score;

    private final Map<String, Integer> opponentScores;

    public Player (ClientHandler clientHandler){
        this.clientHandler = clientHandler;
        offeredMatches = new HashSet<>();
        offeredRematches = new HashSet<>();
        name = new String();
        opponentScores = new java.util.HashMap<>();
    }
//    public int getScore(){
//        return score;
//    }
//    public void clearScore(){
//        score = 0;
//    }
//    public void incrementScore(){
//        score += 1;
//    }


    // Získej skóre tohoto hráče proti zadanému soupeři (pokud není, vrátí 0)
    public synchronized int getScoreAgainst(Player opponent) {
        if (opponent == null) return 0;
        String oppName = opponent.getName();
        return opponentScores.getOrDefault(oppName, 0);
    }

    // Inkrementuj skóre tohoto hráče proti zadanému soupeři
    public synchronized void incrementScoreAgainst(Player opponent) {
        if (opponent == null) return;
        String oppName = opponent.getName();
        int old = opponentScores.getOrDefault(oppName, 0);
        opponentScores.put(oppName, old + 1);
    }

    // Nastaví (přepíše) skóre proti soupeři (užitečné pro reset)
    public synchronized void setScoreAgainst(Player opponent, int value) {
        if (opponent == null) return;
        opponentScores.put(opponent.getName(), value);
    }

    // Vymaže celé head-to-head skóre (např. při odstranění hráče)
    public synchronized void clearOpponentScores() {
        opponentScores.clear();
    }

    // Volitelně: získat mapu (nevracejte interní mutable mapu přímo - tohle jen pro debug)
//    public synchronized Map<String, Integer> getOpponentScoresSnapshot() {
//        return new HashMap<>(opponentScores);
//    }


    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
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

    public synchronized boolean isInWaitingRoom() {
        return isInWaitingRoom;
    }

    public synchronized void setInWaitingRoom(boolean inWaitingRoom) {
        isInWaitingRoom = inWaitingRoom;
    }

    public synchronized void offerRematch(Player player){
        offeredRematches.add(player);
    }
    public synchronized void removeRematch(Player player){
        offeredRematches.remove(player);
    }
    public synchronized boolean wantsRematch(Player player){
        return offeredRematches.contains(player);
    }
    public synchronized void clearOfferedRematches(){
        offeredRematches.clear();
    }
    public synchronized Set<Player> getOfferedRematches(){
        return offeredRematches;
    }


}
