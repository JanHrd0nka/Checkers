package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.Connection;
import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Figure;
import cz.vse.java.checkers.common.Pos;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoardController extends Controller {

    private final Logger logger = LoggerFactory.getLogger(BoardController.class);

    private GameController gameController;

    @FXML
    private StackPane boardContainer;



    private GridPane checkersBoard;


    public void createNewGame(){
        checkersBoard = new GridPane();
        if(gameController == null){
            gameController = new GameController(this);
        }else{
            gameController.setupNewGame();
        }

        logger.info("Drawing starting position.");
        drawPieces();

        boardContainer.getChildren().add(checkersBoard);
    }


    @FXML
    public void initialize() {
        logger.info("Initializing game.");
        createNewGame();


    }


    private void drawEmptyBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Rectangle square = new Rectangle(60, 60);
                if ((row + col) % 2 == 0) {
                    square.setFill(Color.web("#F0D9B5")); // Light square
                } else {
                    square.setFill(Color.web("#B58863")); // Dark square
                }
                checkersBoard.add(square, col, row);

                final Pos pos = new Pos(col, row);

                square.setOnMouseClicked(event -> {
                    handleSquareClick(pos);
                });
            }
        }
    }


    public void drawPieces() {
        drawEmptyBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Pos current = new Pos(row, col);

                Figure figure = gameController.getGame().getPiece(current);

                if (figure != Figure.NONE) {

                    Circle piece = new Circle(24);

                        if (figure == Figure.WHITE_MAN) {
                            if(row == gameController.getSelectedRow() &&
                                    col == gameController.getSelectedCol()){
                                piece.setFill(Color.WHITE.darker());
                            }else{
                                piece.setFill(Color.WHITE);
                            }
                        } else if (figure == Figure.BLACK_MAN) {
                            if(row == gameController.getSelectedRow() &&
                                    col == gameController.getSelectedCol()){
                                piece.setFill(Color.BLACK.brighter());
                            } else{
                                piece.setFill(Color.BLACK);
                            }

                        } else if (figure == Figure.WHITE_KING) {
                            if(row == gameController.getSelectedRow() &&
                                    col == gameController.getSelectedCol()){
                                piece.setFill(Color.NAVAJOWHITE.darker());
                            } else{
                                piece.setFill(Color.NAVAJOWHITE);
                            }
                        } else if (figure == Figure.BLACK_KING) {
                            if(row == gameController.getSelectedRow() &&
                                    col == gameController.getSelectedCol()){
                                piece.setFill(Color.DARKBLUE.brighter());
                            } else{
                                piece.setFill(Color.DARKBLUE);
                            }

                        }

                    // Add the piece to the grid
                    checkersBoard.add(piece, current.y(), current.x());

                    // Center the circle inside the grid cell
                    GridPane.setHalignment(piece, HPos.CENTER);
                    GridPane.setValignment(piece, VPos.CENTER);

                    final Pos pos = current;

                    // Add click listener to the piece
                    piece.setOnMouseClicked(event -> handlePieceClick(pos, figure));

                }


            }
        }
    }


    // --- CLICK HANDLERS ---

    private void handlePieceClick(Pos clickedPos, Figure figure) {
        //boolean valid = (gameController.getGame().getWhiteToMove() == (figure == Figure.WHITE_KING || figure == Figure.WHITE_MAN));
        boolean valid = gameController.isPlayerTurn();
        if(valid) {
            gameController.setSelectedRow(clickedPos.x());
            gameController.setSelectedCol(clickedPos.y());

            drawPieces();
            drawPossibleMoves(clickedPos);
        }

    }

    public void drawPossibleMoves(Pos pos){
        List<List<Pos>> possibleMoves = gameController.getGame().getPossibleMoves(pos);

        for(List<Pos> movePath : possibleMoves){
            if(movePath.size() > 1){
                    Rectangle highlight = new Rectangle(60, 60);
                    highlight.setFill(Color.web("#FFFF00", 0.5)); // Semi-transparent yellow
                    checkersBoard.add(highlight, movePath.getLast().y(), movePath.getLast().x());
                    highlight.setOnMouseClicked(event -> handleSquareClick(movePath.getLast()));

            }
        }
//        for (Pos move : possibleMoves) {
//            Rectangle highlight = new Rectangle(60, 60);
//            highlight.setFill(Color.web("#FFFF00", 0.5)); // Semi-transparent yellow
//            checkersBoard.add(highlight, move.y(), move.x());
//            highlight.setOnMouseClicked(event -> handleSquareClick(move));
//        }
    }

    private void handleSquareClick(Pos pos) {
        // Check if a piece is actually selected AND if the target square is empty
        if (gameController.getSelectedRow() != -1 &&
                gameController.getSelectedCol() != -1 &&
                gameController.getGame().getPiece(pos) == Figure.NONE
        ) {

            try {
                gameController.movePiece(
                        new Pos(gameController.getSelectedRow(), gameController.getSelectedCol()),
                        pos
                );
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid move attempted: " + e.getMessage());
            }

        }
    }

    public GameController getGameController() {
        return gameController;
    }

    public void showResultDialog(boolean isWin, String score) {
        String title = isWin ? "Výhra" : "Prohra";
        String header = isWin ? "Gratulace!" : "Bohužel...";
        String content = isWin ? "Vyhráli jste zápas \nSkóre: " + score : "Prohráli jste zápas.\nSkóre: " + score;

        // vytvoříme dialog s dvěma vlastním tlačítky
        ButtonType backButton = new ButtonType("Back to waiting room");
        ButtonType rematchButton = new ButtonType("Offer rematch");
        Alert alert = new Alert(Alert.AlertType.NONE, content, backButton, rematchButton);
        alert.setTitle(title);
        alert.setHeaderText(header);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == backButton) {
                // Přepnout scénu zpět do waiting room. Načteme FXML a nastavíme novou scénu.
                gameController.returnToWaitingRoom();
            } else if (result.get() == rematchButton) {
                // Požádej server o rematch — pošleme jednoduchý REPLAY požadavek.
                // Pokud chcete, můžete zde poslat i jméno soupeře jako obsah.
                SampleMessageHandler handler = Connection.getInstance().getHandler();
                if (handler != null) {
                    String UID = generateID();
                    boolean sent = handler.send(ClientMessage.REPLAY, UID, "");
                    if (!sent) {
                        logger.warn("Nepodařilo se odeslat rematch request");
                    } else {
                        logger.info("Odeslán rematch request (UID={})", UID);
                    }
                } else {
                    logger.warn("Message handler není dostupný, nelze odeslat rematch");
                }
            }
        }
    }

    public Object getScene() {
        return checkersBoard.getScene().getWindow();
    }
}
