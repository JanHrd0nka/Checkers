package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.Game;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timer {

    private final Set<Game> games = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public Timer() {
        start();
    }

    public void add(Game game) {
        games.add(game);
    }

    public void remove(Game game) {
        games.remove(game);
    }

    private void start() {
        scheduler.scheduleAtFixedRate(this::run, 0, 5, TimeUnit.SECONDS);
    }

    public void run() {
        for (Game g : games) {
            g.sendTime();
        }
    }
    public void shutdown() {
        scheduler.shutdownNow();
    }
}