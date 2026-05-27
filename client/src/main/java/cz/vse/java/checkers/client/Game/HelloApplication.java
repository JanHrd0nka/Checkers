package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.MessageEventBus;
import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.MessageHandler;
import cz.vse.java.checkers.client.Networking.ResponseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** * HelloApplication - inicializátor aplikace. * Nyní POUZE inicializuje UI a wiry dependency injection. * MessageHandler NEMÁ ŽÁDNÉ REFERENCE NA CONTROLLERY. */
public class HelloApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(HelloApplication.class);

    @Override
    public void start(Stage stage) throws IOException {
        // Inicializace singleton komponent
        Connection connection = Connection.getInstance();
        ResponseManager responseManager = ResponseManager.getInstance();
        MessageEventBus eventBus = MessageEventBus.getInstance();

        // Připojení k serveru
        connection.connect("localhost", 5000);

        // Vytvoření MessageHandler - bez přímých referencí na controllery
        MessageHandler handler = new MessageHandler(connection, responseManager, eventBus);
        connection.addMessageHandler(handler);

        // Načtení všech FXML scén
        FXMLLoader initialConnLoader = loadFXML("initialConn-view.fxml");
        FXMLLoader setupLoader = loadFXML("setup-view.fxml");
        FXMLLoader gameLoader = loadFXML("game-view.fxml");
        FXMLLoader waitingRoomLoader = loadFXML("waitingRoom-view.fxml");

        Scene initialConnScene = new Scene(initialConnLoader.load(), 320, 240);
        Scene setupScene = new Scene(setupLoader.load(), 400, 300);
        Scene gameScene = new Scene(gameLoader.load(), 600, 600);
        Scene waitingRoomScene = new Scene(waitingRoomLoader.load(), 400, 300);

        // Získání controllerů z FXML
        InitialConnControler initialConnControler = initialConnLoader.getController();
        initialConnControler.setNextScene(waitingRoomScene);

        WaitingRoomController waitingRoomController = waitingRoomLoader.getController();
        waitingRoomController.setNextScene(setupScene);
        waitingRoomController.setPrevScene(initialConnScene);

        SetupController setupController = setupLoader.getController();
        setupController.setNextScene(gameScene);
        setupController.setPrevScene(waitingRoomScene);

        BoardController boardController = gameLoader.getController();
        GameController gameController = boardController.getGameController();

        gameController.setNextScene(waitingRoomScene);
        gameController.setPrevScene(setupScene);

        // Připojení MessageHandleru do Connection (aby mohl přijímat zprávy)
        // Controllery se registrují v eventBusu sami v initialize() metodě

        // Inicializace stage
        stage.setTitle("Welcome");
        stage.setScene(initialConnScene);
        stage.setOnCloseRequest(event -> {
            logger.info("Closing application");
            Connection.getInstance().disconnect();
        });
        stage.show();
    }

    private FXMLLoader loadFXML(String filename) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getClassLoader().getResource(filename)
        );
        if (loader.getLocation() == null) {
            throw new IOException("FXML file not found: " + filename);
        }
        return loader;
    }

    public static void main(String[] args) {
        launch();
    }
}