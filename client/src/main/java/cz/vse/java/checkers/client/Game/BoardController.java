package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.client.Networking.SampleMessageHandler;
import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Figure;
import cz.vse.java.checkers.common.Pos;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BoardController {

    private final Logger logger = LoggerFactory.getLogger(BoardController.class);

    private GameController gameController;

    @FXML
    private StackPane boardContainer;
    @FXML
    private Button offerMatch;
    @FXML
    private Button loginButton;
    @FXML
    private TextField loginName;



    private GridPane checkersBoard;



    @FXML
    public void initialize() {
        logger.info("Initializing game.");
        checkersBoard = new GridPane();
        gameController = new GameController(true, this);
        gameController.setupStartingPositions();

        logger.info("Drawing starting position.");
        drawPieces();

        boardContainer.getChildren().add(checkersBoard);


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

                final Pos pos = new Pos(row, col);

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
        boolean valid = (gameController.getGame().getWhiteToMove() == (figure == Figure.WHITE_KING || figure == Figure.WHITE_MAN));
        if(valid) {
            gameController.setSelectedRow(clickedPos.x());
            gameController.setSelectedCol(clickedPos.y());

            drawPieces();
            drawPossibleMoves(clickedPos, figure);
        }

    }

    public void drawPossibleMoves(Pos pos, Figure figure){
        List<Pos> possibleMoves = gameController.getGame().getPossibleMoves(pos);


        for (Pos move : possibleMoves) {
            Rectangle highlight = new Rectangle(60, 60);
            highlight.setFill(Color.web("#FFFF00", 0.5)); // Semi-transparent yellow
            checkersBoard.add(highlight, move.y(), move.x());
            highlight.setOnMouseClicked(event -> handleSquareClick(move));
        }
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
}
