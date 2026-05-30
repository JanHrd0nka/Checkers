package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.Game;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeSender {
    private final Set<TimedGame> games = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public TimeSender() {
        start();
    }

    public void add(TimedGame game) {
        games.add(game);
    }

    public void remove(TimedGame game) {
        games.remove(game);
    }

    private void start() {
        scheduler.scheduleAtFixedRate(this::run, 0, 5, TimeUnit.SECONDS);
    }

    public void run() {
        for (TimedGame g : games) {
            g.sendTimes();
        }
    }
    public void shutdown() {
        scheduler.shutdownNow();
    }
}