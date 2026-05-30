package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.Game;
import cz.vse.java.checkers.common.Pos;
import cz.vse.java.checkers.common.ServerMessage;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TimedGame extends Game {
    private final Player white;
    private final Player black;

    private long whiteRemainingTimeMsec;
    private long blackRemainingTimeMsec;
    private long lastUpdateMsec;
    private boolean isFinished;

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
        isFinished = false;
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
        if (!isFinished)
        {
            updateTimes();
            int whiteTime = (int) whiteRemainingTimeMsec / 1000;
            int blackTime = (int) whiteRemainingTimeMsec / 1000;
            white.getClientHandler().send(ServerMessage.TIME, "server-id " + whiteTime);
            black.getClientHandler().send(ServerMessage.TIME, "server-id " + blackTime);
        }
    }

    private synchronized void updateTimes(){
        long now = System.currentTimeMillis();
        long delta = now - lastUpdateMsec;
        if (isWhiteToMove()) {
            whiteRemainingTimeMsec -= delta;
            if (whiteRemainingTimeMsec < 0) {
                white.getClientHandler().sendResult(white, false);
                isFinished = true;
            }
        } else {
            blackRemainingTimeMsec -= delta;
            if (blackRemainingTimeMsec < 0) {
                black.getClientHandler().sendResult(black, false);
                isFinished = true;
            }
        }
        lastUpdateMsec = now;
    }
}