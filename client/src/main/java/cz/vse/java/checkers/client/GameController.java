package cz.vse.java.checkers.client;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class GameController {

    @FXML
    private StackPane boardContainer;

    @FXML
    private StackPane bottomContainer;

    private GridPane checkersBoard;

    // --- THE MODEL ---
    // 0 = Empty, 1 = Player 1 (Red), 2 = Player 2 (Black)
    // You could also use an Enum here, which is even better!
    private int[][] boardState = new int[8][8];


    // Variables to remember which piece the user clicked
    private int selectedRow = -1;
    private int selectedCol = -1;

    @FXML
    public void initialize() {
        checkersBoard = new GridPane();

        drawEmptyBoard();
        setupStartingPositions();
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
    }

    // 3. Read the data array and draw the pieces (The View)
    private void drawPieces() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                int pieceValue = boardState[row][col];

                if (pieceValue != 0) {
                    // Create a circle with a radius of 24 (fits nicely inside 60x60 square)
                    Circle piece = new Circle(24);

                    if (pieceValue == 1) {
                        piece.setFill(Color.DARKRED);
                    } else if (pieceValue == 2) {
                        piece.setFill(Color.BLACK);
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
                    piece.setOnMouseClicked(event -> handlePieceClick(r, c));

                }
            }
        }
    }

    // --- CLICK HANDLERS ---

    private void handlePieceClick(int row, int col) {
        // Save the coordinates of the clicked piece
        selectedRow = row;
        selectedCol = col;

        System.out.println("Selected piece row: " + selectedRow + " col: " + selectedCol);

        // Redraw the pieces so the selected one gets the "darker" color
        drawPieces();
    }

    private void handleSquareClick(int row, int col) {
        // Check if a piece is actually selected AND if the target square is empty
        if (selectedRow != -1 && selectedCol != -1 && boardState[row][col] == 0) {

            // 1. Move the piece in the underlying data array
            boardState[row][col] = boardState[selectedRow][selectedCol];

            // 2. Erase the piece from its old position in the data array
            boardState[selectedRow][selectedCol] = 0;

            // 3. Deselect the piece
            selectedRow = -1;
            selectedCol = -1;

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    System.out.println(boardState[i][j]);
                }
            }

            // 4. Redraw the board to reflect the new array state
            drawEmptyBoard();
            drawPieces();
        }
    }
}