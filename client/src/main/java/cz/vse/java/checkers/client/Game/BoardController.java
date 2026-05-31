package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Figure;
import cz.vse.java.checkers.common.Pos;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * BoardController - odpovídá za UI vykreslování desky. * Deleguje herní logiku GameControlleru.
 *
 * @author Adam Filinger
 * @version 1.0
 *
 */
public class BoardController extends Controller {

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 60;
    private static final int PIECE_RADIUS = 24;

    private Stage stage;

    private GameController gameController;
    private GridPane checkersBoard;
    private Alert resultDialog;
    private Alert drawAlert;

    @FXML
    private StackPane boardContainer;
    @FXML
    private Button forfeightBtn;
    @FXML
    private Button drawBtn;
    @FXML
    private Label timeLabel;

    private Group boardWrapper;


    /**
     * Inicializace scény
     */
    @FXML
    public void initialize() {
        logger.info("Initializing board");
        gameController = new GameController(this);
        forfeightBtn.setOnAction(actionEvent -> forfeitMatch());
        drawBtn.setOnAction(actionEvent -> offerDraw());
    }


    /**
     * Vytvořit novou hru
     */
    public void createNewGame(boolean isWhite, boolean mustTake) {
        checkersBoard = new GridPane();
        gameController.setupNewGame(isWhite, mustTake);

        logger.info("Drawing starting position");
        drawPieces();

        if (boardContainer.getChildren().isEmpty()) {
            boardWrapper = new Group(checkersBoard);
            boardContainer.getChildren().add(boardWrapper);
            checkersBoard.setRotate(gameController.isWhite() ? 90 : -90);
        }
    }

    /**
     * Reset UI desky - vymazat vybrané pozice a překreslit
     */
    public void resetBoardDisplay() {
        logger.info("Resetting board display");
        gameController.setSelectedRow(-1);
        gameController.setSelectedCol(-1);
        boardContainer.getChildren().remove(boardWrapper);
        drawPieces();
    }


    /**
     * Vykreslit kousky na desku
     */
    public void drawPieces() {
        drawEmptyBoard();
        if (!gameController.isWhite()) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                for (int row = 0; row < BOARD_SIZE; row++) {
                    Pos current = new Pos(row, col);
                    Figure figure = gameController.getGame().getPiece(current);

                    if (figure != Figure.NONE) {
                        drawPiece(current, figure);
                    }
                }
            }
        } else {
            for (int col = BOARD_SIZE - 1; col >= 0; col--) {
                for (int row = BOARD_SIZE - 1; row >= 0; row--) {
                    Pos current = new Pos(row, col);
                    Figure figure = gameController.getGame().getPiece(current);

                    if (figure != Figure.NONE) {
                        drawPiece(current, figure);
                    }
                }
            }
        }
    }

    /**
     * Vykreslit jednu figurku
     */
    private void drawPiece(Pos pos, Figure figure) {
        Circle piece = new Circle(PIECE_RADIUS);
        piece.setFill(getFigureColor(figure, pos.x(), pos.y()));

        checkersBoard.add(piece, pos.x(), pos.y());
        GridPane.setHalignment(piece, HPos.CENTER);
        GridPane.setValignment(piece, VPos.CENTER);

        piece.setOnMouseClicked(event -> handlePieceClick(pos));
    }

    /**
     * Získat barvu pro figurku
     */
    private Color getFigureColor(Figure figure, int row, int col) {
        boolean isSelected = (row == gameController.getSelectedRow() &&
                col == gameController.getSelectedCol());

        return switch (figure) {
            case WHITE_MAN -> isSelected ? Color.WHITE.darker() : Color.WHITE;
            case BLACK_MAN -> isSelected ? Color.BLACK.brighter() : Color.BLACK;
            case WHITE_KING -> isSelected ? Color.NAVAJOWHITE.darker() : Color.NAVAJOWHITE;
            case BLACK_KING -> isSelected ? Color.DARKBLUE.brighter() : Color.DARKBLUE;
            default -> Color.TRANSPARENT;
        };
    }

    /**
     * Vykreslit prázdnou desku (šachovnici)
     */
    private void drawEmptyBoard() {
        checkersBoard.getChildren().clear();
        for (int col = 0; col < BOARD_SIZE; col++) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                square.setFill((row + col) % 2 == 0
                        ? Color.web("#F0D9B5")  // Light square
                        : Color.web("#B58863")  // Dark square
                );

                Pos pos = new Pos(row, col);
                square.setOnMouseClicked(event -> handleSquareClick(pos));
                checkersBoard.add(square, row, col);
            }
        }
    }

    /**
     * Obsluha kliknutí na figurku
     */
    private void handlePieceClick(Pos clickedPos) {
        if (!gameController.isPlayerTurn()) {
            logger.debug("Not player turn");
            return;
        }
        logger.debug("Clicked piece on: {}", clickedPos.x() + ":" + clickedPos.y());

        gameController.setSelectedRow(clickedPos.x());
        gameController.setSelectedCol(clickedPos.y());

        drawPieces();
        drawPossibleMoves(clickedPos);
    }

    /**
     * Vykreslit možné tahy
     */
    public void drawPossibleMoves(Pos pos) {
        List<List<Pos>> possibleMoves = gameController.getGame().getPossibleMoves(pos);

        for (List<Pos> movePath : possibleMoves) {
            if (!movePath.isEmpty()) {
                Pos targetPos = movePath.getLast();
                Rectangle highlight = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                highlight.setFill(Color.web("#FFFF00", 0.5)); // Semi-transparent yellow

                checkersBoard.add(highlight, targetPos.x(), targetPos.y());
                highlight.setOnMouseClicked(event -> handleSquareClick(targetPos));
            }
        }
    }

    /**
     * Obsluha kliknutí na pole
     */
    private void handleSquareClick(Pos pos) {
        if (gameController.getSelectedRow() != -1 &&
                gameController.getSelectedCol() != -1 &&
                gameController.getGame().getPiece(pos) == Figure.NONE) {

            try {
                Pos fromPos = new Pos(gameController.getSelectedRow(), gameController.getSelectedCol());
                gameController.movePiece(fromPos, pos);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid move: {}", e.getMessage());
            }
        }
    }

    /**
     * Zobrazit dialog s výsledkem hry
     */
    public void showResultDialog(String ID, boolean isWin, String score) {
        drawBtn.setDisable(false);
        forfeightBtn.setDisable(false);
        String title = isWin ? "Win" : "Lost";
        String header = isWin ? "Congratulations!" : "Its too bad...";
        String content;

        if (ID.equals("surrender")) {
            content = "Your opponent has surrendered\nScore: " + score;
        } else {
            content = isWin
                    ? "You won the match \nScore: " + score
                    : "You lost the match.\nScore: " + score;
        }
        ButtonType backButton = new ButtonType("Back to waiting room");
        ButtonType rematchButton = new ButtonType("Offer rematch");
        resultDialog = new Alert(Alert.AlertType.NONE, content, backButton, rematchButton);
        resultDialog.setTitle(title);
        resultDialog.setHeaderText(header);

        Optional<ButtonType> result = resultDialog.showAndWait();
        if (result.isPresent()) {
            String UID = generateID();
            if (result.get() == backButton) {
                gameController.returnToWaitingRoom();
            } else if (result.get() == rematchButton) {
                gameController.sendRematchMessage(ClientMessage.REPLAY, UID, "yes");
            }
        }
    }

    public void closeResultDialog() {
        if (resultDialog.isShowing()) {
            logger.info("Closing result dialog");
            resultDialog.close();
        }
    }

    private void forfeitMatch() {
        String UID = generateID();
        gameController.sendForfeitMessage(ClientMessage.SURRENDER, UID, "");
    }

    private void offerDraw() {
        String UID = generateID();
        gameController.sendDrawRequest(ClientMessage.DRAW, UID, "");
        drawBtn.setDisable(true);
        forfeightBtn.setDisable(true);
    }


    public GameController getGameController() {
        return gameController;
    }

    public Stage getStage() {
        return this.stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Zobrazení dialogového okna při příchozí nabídce na remízu
     */
    public void displayDraw() {
        drawBtn.setDisable(true);
        forfeightBtn.setDisable(true);
        ButtonType decline = new ButtonType("Odmíntout");
        ButtonType accept = new ButtonType("Přijmout");

        drawAlert = new Alert(Alert.AlertType.NONE, "", decline, accept);
        drawAlert.setTitle("Remíza");
        drawAlert.setHeaderText("Soupeř nabídl remízu");


        Optional<ButtonType> result = drawAlert.showAndWait();
        if (result.isPresent() && result.get() == decline) {
            gameController.sendDrawOffer(false);
            drawBtn.setDisable(false);
            forfeightBtn.setDisable(false);
            logger.info("Sending draw decline");
        } else if (result.isPresent() && result.get() == accept) {
            gameController.sendDrawOffer(true);
            drawBtn.setDisable(false);
            forfeightBtn.setDisable(false);
            logger.info("Sending draw accept");
        }

    }

    public void drawDeclined() {
        drawAlert = new Alert(Alert.AlertType.INFORMATION, "Soupeř odmítl remízu", ButtonType.OK);
        drawAlert.setTitle("Remíza odmítnuta");
        drawAlert.onCloseRequestProperty().set(event -> {
            drawBtn.setDisable(false);
            forfeightBtn.setDisable(false);
        });
        drawAlert.show();
    }

    public void updateTime(String s) {
        String current = timeLabel.getText();
        if (!Objects.equals(current, s)) {
            Platform.runLater(() -> timeLabel.setText(s));
        }
    }
}
