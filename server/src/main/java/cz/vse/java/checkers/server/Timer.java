package cz.vse.java.checkers.server;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timer {

    private final Set<Player> players = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public Timer() {
        start();
    }

    public void add(Player p1, Player p2) {
        players.add(p1);
        players.add(p2);
    }

    public void remove(Player p1, Player p2) {
        players.remove(p1);
        players.remove(p2);
    }

    private void start() {
        scheduler.scheduleAtFixedRate(this::run, 0, 5, TimeUnit.SECONDS);
    }

    public void run() {
        for (Player p : players) {

//            try {
//                p.getClientHandler().sendTime();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }
    public void shutdown() {
        scheduler.shutdownNow();
    }
}