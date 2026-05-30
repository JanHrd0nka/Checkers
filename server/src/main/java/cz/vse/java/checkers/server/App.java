package cz.vse.java.checkers.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private Server server;
    private final Logger log = LoggerFactory.getLogger(App.class);

    public void start() {
        log.info("Starting application...");

        server = new Server(5000);
        server.start();
    }
}