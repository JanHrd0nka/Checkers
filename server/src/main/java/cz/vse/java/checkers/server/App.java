package cz.vse.java.checkers.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Server application bootstrap class.
 * Handles startup initialization, obtains the server port
 * from the user, and starts the server instance.
 *
 * @author Jan Hrdonka
 * @version 1.0
 */
public class App {
    private final Logger log = LoggerFactory.getLogger(App.class);

    public void start() {
        log.info("Starting application...");

        Scanner scanner = new Scanner(System.in);
        int port;

        while (true) {
            try {
                System.out.print("Select port: ");
                port = Integer.parseInt(scanner.nextLine());
                if (port < 1 || port > 65535) {
                    System.out.println("Port must be between 1 and 65535.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Please enter a number.");
            }
        }

        Server server = new Server(port);
        server.start();
    }
}