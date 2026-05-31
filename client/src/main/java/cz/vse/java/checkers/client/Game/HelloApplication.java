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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/** * HelloApplication - inicializátor aplikace.
 * @author Adam Filinger
 * @version 1.0
 * */
public class HelloApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(HelloApplication.class);

    /**
     * Nastavení všech scén aplikace a binding controllerů
     * @param stage
     */
    @Override
    public void start(Stage stage){
        // Inicializace singleton komponent

        String[] config = loadConfig();
        String host = config[0].isEmpty() ? "N/A" : config[0];
        int port = config[1].isEmpty() ? -1 : Integer.parseInt(config[1]);


        FXMLLoader initialConnLoader;
        FXMLLoader setupLoader;
        FXMLLoader gameLoader;
        FXMLLoader waitingRoomLoader;
        Scene initialConnScene;
        Scene setupScene;
        Scene gameScene;
        Scene waitingRoomScene;
        try {
            Connection connection = Connection.getInstance();
            ResponseManager responseManager = ResponseManager.getInstance();
            MessageEventBus eventBus = MessageEventBus.getInstance();

            if(!host.equals("N/A") && port != -1){
                // Připojení k serveru
                connection.connect(host, port);
            }else{
                logger.debug("No configuration found in config file");
            }


            // Vytvoření MessageHandler - bez přímých referencí na controllery
            MessageHandler handler = new MessageHandler(connection, responseManager, eventBus);
            connection.addMessageHandler(handler);

            // Načtení všech FXML scén
            initialConnLoader = loadFXML("initialConn-view.fxml");
            setupLoader = loadFXML("setup-view.fxml");
            gameLoader = loadFXML("game-view.fxml");
            waitingRoomLoader = loadFXML("waitingRoom-view.fxml");

            initialConnScene = new Scene(initialConnLoader.load(), 320, 240);
            setupScene = new Scene(setupLoader.load(), 400, 300);
            gameScene = new Scene(gameLoader.load(), 600, 600);
            waitingRoomScene = new Scene(waitingRoomLoader.load(), 400, 300);
        } catch (IOException e) {
            logger.warn("Failed to load FXML files, application cannot start", e);
            return;
        }

        // Získání controllerů z FXML
        InitialConnControler initialConnControler = initialConnLoader.getController();
        initialConnControler.setNextScene(waitingRoomScene);
        initialConnControler.setStage(stage);
        initialConnControler.setThisScene(initialConnScene);

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
        boardController.setStage(stage);

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

    /**
     * Načítání parametrů připojení z konfiguračního souboru
     * @return String[] parametry připojení
     */
    private String[] loadConfig(){
        String host = "";
        String port = "";
        try{
            Document conf = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse((
                    HelloApplication.class.getClassLoader().getResourceAsStream("META-INF/config.xml")
            ));
            conf.getDocumentElement().normalize();

            Node serverNode = conf.getElementsByTagName("server").item(0);
            if(serverNode.getNodeType() == Node.ELEMENT_NODE){
                Element serverElement = (Element) serverNode;
                host = serverElement.getElementsByTagName("host").item(0).getTextContent();
                logger.debug("Loaded host from config: {}", host);
                port = serverElement.getElementsByTagName("port").item(0).getTextContent();
                logger.debug("Loaded port from config: {}", port);
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.warn("Failed to parse config.xml, using default configuration", e);
        }
        return new String[]{host, port};
    }

}