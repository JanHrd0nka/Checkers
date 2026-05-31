package cz.vse.java.checkers.server;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically sends remaining time updates for all active timed games.
 * <p>
 * The TimeSender maintains a thread-safe set of running TimedGame instances
 * and uses a scheduled executor to broadcast time updates to both players
 * at a fixed interval (once per second).
 * <p>
 * It is also responsible for removing finished games from time tracking.
 *
 * @author Jan Hrdonka
 * @version 1.0
 */
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
        if (game != null) {
            games.remove(game);
        }
    }

    private void start() {
        scheduler.scheduleAtFixedRate(this::run, 0, 1, TimeUnit.SECONDS);
    }

    public void run() {
        for (TimedGame g : games) {
            g.sendTimes();
        }
    }
}