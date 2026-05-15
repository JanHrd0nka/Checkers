package cz.vse.java.checkers.client.Game;

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

//    @FXML
//    public void initialize() {
//        logger.info("Initializing game.");
//        checkersBoard = new GridPane();
//        gameController = new GameController(new InitialConnControler(), true);
//        gameController.setupStartingPositions();
//        logger.info("Setting up starting position.");
//        logger.info("Drawing starting position.");
//        drawPieces();
//
//        boardContainer.getChildren().add(checkersBoard);
//
//
//    }

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

                final int r = row;
                final int c = col;

                square.setOnMouseClicked(event -> {
                    handleSquareClick(r, c);
                });
            }
        }
    }


    private void drawPieces() {
        drawEmptyBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Pos current = new Pos(row, col);

                Figure figure = gameController.getGame().getPiece(new Pos(row, col));
                //int pieceValue = boardState[row][col];

                if (figure != Figure.NONE) {

                    // Create a circle with a radius of 24 (fits nicely inside 60x60 square)
                    Circle piece = new Circle(24);

                        if (figure == Figure.WHITE_MAN) {
                            if(row == gameController.getSelectedRow() &&
                                    col == gameController.getSelectedCol()){
                                piece.setFill(Color.RED);
                            }else{
                                piece.setFill(Color.DARKRED);
                            }
                        } else if (figure == Figure.BLACK_MAN) {
                            if(row == gameController.getSelectedRow() &&
                                    col == gameController.getSelectedCol()){
                                piece.setFill(Color.GREEN);
                            } else{
                                piece.setFill(Color.BLACK);
                            }

                        } else if (figure == Figure.WHITE_KING) {
                            if(row == gameController.getSelectedRow() &&
                                    col == gameController.getSelectedCol()){
                                piece.setFill(Color.PINK);
                            } else{
                                piece.setFill(Color.RED);
                            }
                        } else if (figure == Figure.BLACK_KING) {
                            if(row == gameController.getSelectedRow() &&
                                    col == gameController.getSelectedCol()){
                                piece.setFill(Color.LIGHTGRAY);
                            } else{
                                piece.setFill(Color.GRAY);
                            }

                        }


                    // Optional: Add a slight border to make the pieces pop
                    piece.setStroke(Color.WHITE);
                    piece.setStrokeWidth(2);

                    // Add the piece to the grid
                    checkersBoard.add(piece, col, row);

                    // Center the circle inside the grid cell
                    GridPane.setHalignment(piece, HPos.CENTER);
                    GridPane.setValignment(piece, VPos.CENTER);

                    final int r = row;
                    final int c = col;

                    // Add click listener to the piece
                    piece.setOnMouseClicked(event -> handlePieceClick(r, c, figure));

                }


            }
        }
    }


    // --- CLICK HANDLERS ---

    private void handlePieceClick(int row, int col, Figure figure) {
        // Save the coordinates of the clicked piece
        boolean valid = (gameController.getGame().getWhiteToMove() == (figure == Figure.WHITE_KING || figure == Figure.WHITE_MAN));
        if(valid) {
            gameController.setSelectedRow(row);
            gameController.setSelectedCol(col);

            // Redraw the pieces so the selected one gets the "darker" color
            drawPieces();
            drawPossibleMoves(row, col, figure);
        }

    }

    public void drawPossibleMoves(int row, int col, Figure figure){
        List<Pos> possibleMoves = gameController.getGame().getPossibleMoves(new Pos(row, col));

        for (Pos move : possibleMoves) {
            Rectangle highlight = new Rectangle(60, 60);
            highlight.setFill(Color.web("#FFFF00", 0.5)); // Semi-transparent yellow
            checkersBoard.add(highlight, move.y(), move.x());
            highlight.setOnMouseClicked(event -> handleSquareClick(move.x(), move.y()));
        }
    }

    private void handleSquareClick(int row, int col) {
        // Check if a piece is actually selected AND if the target square is empty
        if (gameController.getSelectedRow() != -1 &&
                gameController.getSelectedCol() != -1 &&
                gameController.getGame().getPiece(new Pos(row, col)) == Figure.NONE
        ) {

            // Move piece in game instance
            try {
                gameController.getGame().movePiece(
                        new Pos(gameController.getSelectedRow(), gameController.getSelectedCol()),
                        new Pos(row, col)
                );
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid move attempted: " + e.getMessage());
                return; // Don't proceed with the move if it's invalid
            }
            // 2. Erase the piece from its old position in the data array
            //boardState[selectedRow][selectedCol] = 0;

            // 3. Deselect the piece
            gameController.setSelectedRow(-1);
            gameController.setSelectedCol(-1);

            // 4. Redraw the board to reflect the new array state
            drawPieces();
        }
    }




}
