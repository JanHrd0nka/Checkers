package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.Game;
import cz.vse.java.checkers.common.Pos;
import cz.vse.java.checkers.common.ServerMessage;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TimedGame extends Game {

    public interface TimeoutListener {
        void onTimeout(Player loser);
    }
    private final Player white;
    private final Player black;

    private long whiteRemainingTimeMsec;
    private long blackRemainingTimeMsec;
    private long lastUpdateMsec;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public TimedGame(boolean mustTake,
                     Player white,
                     Player black,
                     int timeLimitSeconds)
    {
        super(mustTake);
        this.white = white;
        this.black = black;
        whiteRemainingTimeMsec = (long)timeLimitSeconds * 1000;
        blackRemainingTimeMsec = (long)timeLimitSeconds * 1000;
        lastUpdateMsec = System.currentTimeMillis();
    }

    @Override
    public synchronized boolean makeMove(List<Pos> path){
        updateTimes();
        return super.makeMove(path);
    }
    @Override
    public synchronized boolean checkGameState() {
        updateTimes();
        if (isWhiteToMove() && whiteRemainingTimeMsec < 0){
            return false;
        }
        else if (blackRemainingTimeMsec < 0){
            return false;
        }
        return super.checkGameState();
    }

    public synchronized void sendTimes(){
        updateTimes();
        white.getClientHandler().send(ServerMessage.TIME, "server-id " + whiteRemainingTimeMsec + " " + blackRemainingTimeMsec);
        black.getClientHandler().send(ServerMessage.TIME, "server-id " + whiteRemainingTimeMsec + " " + blackRemainingTimeMsec);
    }

    private synchronized void updateTimes(){
        long now = System.currentTimeMillis();
        long delta = now - lastUpdateMsec;
        if (isWhiteToMove()){
            whiteRemainingTimeMsec -= delta;
        }
        else {
            blackRemainingTimeMsec -= delta;
        }
        lastUpdateMsec = now;
    }
}