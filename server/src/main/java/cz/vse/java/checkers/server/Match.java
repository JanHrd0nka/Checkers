package cz.vse.java.checkers.server;

public class Match {
    private final Player p1;
    private final Player p2;

    public Match (Player p1, Player p2){
        this.p1 = p1;
        this.p2 = p2;
        p1.setMatch(this);
        p2.setMatch(this);
    }
}
