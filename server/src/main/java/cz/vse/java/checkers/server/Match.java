package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.Pos;

import java.util.List;

public class Match {
    private final Player p1;
    private final Player p2;
    private TimedGame game;
    private Player currentPlayer;
    private boolean drawOffered;
    public Match (Player p1, Player p2){
        this.p1 = p1;
        this.p2 = p2;
        p1.setMatch(this);
        p2.setMatch(this);
    }
    public synchronized void setUp(int time, Player white, boolean mustTake, TimeSender timeSender){
        currentPlayer = white;
        var black = getOpponent(white);
        if (game != null){
            timeSender.remove(game);
        }
        game = new TimedGame(mustTake, white, black, time);
        timeSender.add(game);
    }

    public synchronized boolean isSetup(){
        return game != null;
    }

    public synchronized Player getOpponent(Player player){
        if (player == p1){
            return p2;
        }
        else{
            return p1;
        }
    }
    public String getGameContent(){
        return game.toContent();
    }
    public String getHistory(int index){
        return game.getHistory(index);
    }
    public String makeMove(Player player, List<Pos> path){
        String error = "";
        if (player == currentPlayer){
            if (game.makeMove(path)){
                switchPlayersTurn();
            }
            else{
                error = " Neplarny_tah";
            }
        }
        else{
            error = " Nejsi_na_tahu.";
        }
        return error;
    }
    private void switchPlayersTurn(){
        if (currentPlayer == p1){
            currentPlayer = p2;
        }
        else {
            currentPlayer = p1;
        }
    }
    public boolean checkGameState (){
        return game.checkGameState();
    }

    public Player getWinner(){
        return getOpponent(currentPlayer);
    }

    public boolean isDrawOffered() {
        return drawOffered;
    }

    public void setDrawOffered(boolean drawOffered) {
        this.drawOffered = drawOffered;
    }

    public void endGame(){
        game = null;
    }

    public TimedGame getGame(){
        return game;
    }
}
