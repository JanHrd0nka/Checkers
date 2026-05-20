package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Set;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Connection connection = Connection.getInstance();
        connection.connect("localhost", 5000);

        SampleMessageHandler handler = new SampleMessageHandler(connection);
        connection.addMessageHandler(handler);


        FXMLLoader initialConnLoader = new FXMLLoader(HelloApplication.class.getClassLoader().getResource("initialConn-view.fxml"));
        FXMLLoader setupLoader = new FXMLLoader(HelloApplication.class.getClassLoader().getResource("setup-view.fxml"));
        FXMLLoader gameLoader = new FXMLLoader(HelloApplication.class.getClassLoader().getResource("game-view.fxml"));
        FXMLLoader waitingRoomLoader = new FXMLLoader(HelloApplication.class.getClassLoader().getResource("waitingRoom-view.fxml"));


        Scene initialConnScene = new Scene(initialConnLoader.load(), 320, 240);
        Scene setupScene = new Scene(setupLoader.load(), 400, 300);
        Scene gameScene = new Scene(gameLoader.load(), 600, 600);
        Scene waitingRoomScene = new Scene(waitingRoomLoader.load(), 400, 300);




        InitialConnControler initialConnControler = initialConnLoader.getController();
        initialConnControler.setNextScene(waitingRoomScene);

        WaitingRoomController waitingRoomController = waitingRoomLoader.getController();
        waitingRoomController.setNextScene(setupScene);

        SetupController setupController = setupLoader.getController();
        setupController.setNextScene(gameScene);


        BoardController boardController = gameLoader.getController();
        GameController gameController = boardController.getGameController();

        handler.registerController(waitingRoomController);
        handler.registerController(initialConnControler);

        handler.setInitialController(initialConnControler);
        handler.setWaitingRoomController(waitingRoomController);
        handler.setSetupController(setupController);
        handler.setGameController(gameController);


        stage.setTitle("Welcome");
        stage.setScene(initialConnScene);
        stage.setOnCloseRequest(event -> {
            Connection.getInstance().disconnect();
        });
        stage.show();




    }
}
