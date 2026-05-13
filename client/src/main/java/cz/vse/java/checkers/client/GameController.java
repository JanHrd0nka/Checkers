package cz.vse.java.checkers.client;

import cz.vse.java.checkers.common.Game;
import cz.vse.java.checkers.common.Pos;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import cz.vse.java.checkers.common.Figure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GameController {

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    @FXML
    private StackPane boardContainer;

    @FXML
    private StackPane bottomContainer;

    private GridPane checkersBoard;

    // --- THE MODEL ---
    // 0 = Empty, 1 = Player 1 (Red), 2 = Player 2 (Black)
    // You could also use an Enum here, which is even better!
    private int[][] boardState = new int[8][8];
    private final Game game = new Game(true);

    // Variables to remember which piece the user clicked
    private int selectedRow = -1;
    private int selectedCol = -1;

    @FXML
    public void initialize() {
        logger.info("Initializing game.");
        checkersBoard = new GridPane();

        logger.info("Setting up starting position.");
        setupStartingPositions();
        logger.info("Drawing starting position.");
        drawPieces();

        boardContainer.getChildren().add(checkersBoard);
    }

    // 1. Draw the visual background squares
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

    // 2. Set up the underlying data (The Model)
    private void setupStartingPositions() {
       /*
        ----OLD---
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // In standard checkers, pieces only go on the dark squares
                if ((row + col) % 2 != 0) {
                    if (row < 3) {

                        boardState[row][col] = 1; // Player 1 (Top 3 rows)
                    } else if (row > 4) {
                        boardState[row][col] = 2; // Player 2 (Bottom 3 rows)
                    } else {
                        boardState[row][col] = 0; // Empty middle rows
                    }
                } else {
                    boardState[row][col] = 0; // Light squares are always empty
                }
            }
        }
        -----------
        */
        game.setPieces();
    }

    // 3. Read the data array and draw the pieces (The View)
    private void drawPieces() {
        drawEmptyBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Figure figure = game.getPiece(new Pos(row, col));
                //int pieceValue = boardState[row][col];

                if (figure != Figure.NONE) {

                    // Create a circle with a radius of 24 (fits nicely inside 60x60 square)
                    Circle piece = new Circle(24);

                    if (figure == Figure.WHITE_MAN) {
                        if(row == selectedRow && col == selectedCol){
                            piece.setFill(Color.RED);
                        }else{
                            piece.setFill(Color.DARKRED);
                        }
                    } else if (figure == Figure.BLACK_MAN) {
                        if(row == selectedRow && col == selectedCol){
                            piece.setFill(Color.GREEN);
                        } else{
                            piece.setFill(Color.BLACK);
                        }

                    } else if (figure == Figure.WHITE_KING) {
                        if(row == selectedRow && col == selectedCol){
                            piece.setFill(Color.PINK);
                        } else{
                            piece.setFill(Color.RED);
                        }
                    } else if (figure == Figure.BLACK_KING) {
                        if(row == selectedRow && col == selectedCol){
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

    public void drawPossibleMoves(int row, int col, Figure figure){
        List<Pos> possibleMoves = game.getPossibleMoves(new Pos(row, col));

        for (Pos move : possibleMoves) {
            Rectangle highlight = new Rectangle(60, 60);
            highlight.setFill(Color.web("#FFFF00", 0.5)); // Semi-transparent yellow
            checkersBoard.add(highlight, move.y(), move.x());
            highlight.setOnMouseClicked(event -> handleSquareClick(move.x(), move.y()));
        }
    }

    // --- CLICK HANDLERS ---

    private void handlePieceClick(int row, int col, Figure figure) {
        // Save the coordinates of the clicked piece
        boolean valid = (game.getWhiteToMove() == (figure == Figure.WHITE_KING || figure == Figure.WHITE_MAN));
        if(valid) {
            selectedRow = row;
            selectedCol = col;

            // Redraw the pieces so the selected one gets the "darker" color
            drawPieces();
            drawPossibleMoves(row, col, figure);
        }

    }

    private void handleSquareClick(int row, int col) {
        // Check if a piece is actually selected AND if the target square is empty
        if (selectedRow != -1 && selectedCol != -1 && game.getPiece(new Pos(row, col)) == Figure.NONE) {

            // 1. Move the piece in the underlying data array
            //game.getFigures().get(row).get(col) = game.getFigures().get(selectedRow).get(selectedCol);
            try {
                game.movePiece(new Pos(selectedRow, selectedCol), new Pos(row, col));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid move attempted: " + e.getMessage());
                return; // Don't proceed with the move if it's invalid
            }
            // 2. Erase the piece from its old position in the data array
            //boardState[selectedRow][selectedCol] = 0;

            // 3. Deselect the piece
            selectedRow = -1;
            selectedCol = -1;

            // 4. Redraw the board to reflect the new array state
            drawPieces();
        }
    }
}