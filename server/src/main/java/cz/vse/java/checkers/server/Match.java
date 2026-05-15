package cz.vse.java.checkers.server;

public class Match {
    private final Player p1;
    private final Player p2;
    private boolean isSetup;
    public Match (Player p1, Player p2){
        this.p1 = p1;
        this.p2 = p2;
        p1.setMatch(this);
        p2.setMatch(this);
        isSetup = false;
    }
    public synchronized void setUp(int time, Player white, boolean mustTake){
        isSetup = true;
    }

    public synchronized boolean isSetup(){
        return isSetup;
    }

    public synchronized Player getOpponent(Player player){
        if (player == p1){
            return p2;
        }
        else{
            return p1;
        }
    }
}
