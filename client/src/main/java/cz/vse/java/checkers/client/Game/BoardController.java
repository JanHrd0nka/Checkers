package cz.vse.java.checkers.client.Game;

import cz.vse.java.checkers.common.ClientMessage;
import cz.vse.java.checkers.common.Figure;
import cz.vse.java.checkers.common.Pos;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/** * BoardController - odpovídá za UI vykreslování desky. * Deleguje herní logiku GameControlleru. */
public class BoardController extends Controller {

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 60;
    private static final int PIECE_RADIUS = 24;

    private GameController gameController;
    private GridPane checkersBoard;
    private Alert resultDialog;

    @FXML
    private StackPane boardContainer;
    @FXML
    private Button forfeightBtn;
    @FXML
    private Button drawBtn;



    /**     * Inicializace scény     */
    @FXML
    public void initialize() {
        logger.info("Initializing board");
        createNewGame();
        forfeightBtn.setOnAction(actionEvent -> forgeightMatch());
        drawBtn.setOnAction(actionEvent -> offerDraw());
    }


    /**     * Vytvořit novou hru     */
    public void createNewGame() {
        checkersBoard = new GridPane();
        if (gameController == null) {
            gameController = new GameController(this);
        } else {
            gameController.setupNewGame();
        }

        logger.info("Drawing starting position");
        drawPieces();

        if (boardContainer.getChildren().isEmpty()) {
            boardContainer.getChildren().add(checkersBoard);
        }
    }

    /**     * Reset UI desky - vymazat vybrané pozice a překreslit     * DŮLEŽITÉ: Volat při vstupu do hry nebo na rematch     */
    public void resetBoardDisplay() {
        logger.info("Resetting board display");
        gameController.setSelectedRow(-1);
        gameController.setSelectedCol(-1);
        drawPieces();
    }


    /**     * Vykreslit kousky na desku     */
    public void drawPieces() {
        drawEmptyBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Pos current = new Pos(row, col);
                Figure figure = gameController.getGame().getPiece(current);

                if (figure != Figure.NONE) {
                    drawPiece(current, figure, row, col);
                }
            }
        }
    }

    /**     * Vykreslit jednu figurku     */
    private void drawPiece(Pos pos, Figure figure, int row, int col) {
        Circle piece = new Circle(PIECE_RADIUS);
        piece.setFill(getFigureColor(figure, row, col));

        checkersBoard.add(piece, pos.y(), pos.x());
        GridPane.setHalignment(piece, HPos.CENTER);
        GridPane.setValignment(piece, VPos.CENTER);

        piece.setOnMouseClicked(event -> handlePieceClick(pos, figure));
    }

    /**     * Získat barvu pro figurku     */
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

    /**     * Vykreslit prázdnou desku (šachovnici)     */
    private void drawEmptyBoard() {
        checkersBoard.getChildren().clear();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                square.setFill((row + col) % 2 == 0
                        ? Color.web("#F0D9B5")  // Light square
                        : Color.web("#B58863")  // Dark square
                );

                Pos pos = new Pos(col, row);
                square.setOnMouseClicked(event -> handleSquareClick(pos));
                checkersBoard.add(square, col, row);
            }
        }
    }

    /**     * Obsluha kliknutí na figurku     */
    private void handlePieceClick(Pos clickedPos, Figure figure) {
        if (!gameController.isPlayerTurn()) {
            logger.debug("Not player turn");
            return;
        }

        gameController.setSelectedRow(clickedPos.x());
        gameController.setSelectedCol(clickedPos.y());

        drawPieces();
        drawPossibleMoves(clickedPos);
    }

    /**     * Vykreslit možné tahy     */
    public void drawPossibleMoves(Pos pos) {
        List<List<Pos>> possibleMoves = gameController.getGame().getPossibleMoves(pos);

        for (List<Pos> movePath : possibleMoves) {
            if (movePath.size() > 1) {
                Pos targetPos = movePath.getLast();
                Rectangle highlight = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                highlight.setFill(Color.web("#FFFF00", 0.5)); // Semi-transparent yellow

                checkersBoard.add(highlight, targetPos.y(), targetPos.x());
                highlight.setOnMouseClicked(event -> handleSquareClick(targetPos));
            }
        }
    }

    /**     * Obsluha kliknutí na pole     */
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

    /**     * Zobrazit dialog s výsledkem hry     */
    public void showResultDialog(boolean isWin, String score) {
        String title = isWin ? "Výhra" : "Prohra";
        String header = isWin ? "Gratulace!" : "Bohužel...";
        String content = isWin
                ? "Vyhráli jste zápas \nSkóre: " + score
                : "Prohráli jste zápas.\nSkóre: " + score;

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

    private void forgeightMatch() {
        String UID = generateID();
        gameController.sendForfeitMessage(ClientMessage.SURRENDER, UID, "");
    }

    private void offerDraw(){
        String UID = generateID();
        gameController.sendDrawRequest(ClientMessage.DRAW, UID, "");
    }


    public GameController getGameController() {
        return gameController;
    }

    public Stage getStage() {
        return (Stage) checkersBoard.getScene().getWindow();
    }

    /**     * Cleanup - zavolat když se hra skončí     */
    public void cleanup() {
        if (gameController != null) {
            gameController.cleanup();
        }
    }

    public void displayDraw() {
        ButtonType decline = new ButtonType("Odmíntout");
        ButtonType accept = new ButtonType("Přijmout");

        Alert drawAlert = new Alert(Alert.AlertType.NONE, "",decline, accept);
        drawAlert.setTitle("Remíza");
        drawAlert.setHeaderText("Soupeř nabídl remízu");


        Optional<ButtonType> result = drawAlert.showAndWait();
        if (result.isPresent() && result.get() == decline) {
            gameController.sendDrawOffer(false);
            logger.info("Sending draw decline");
        } else if(result.isPresent() && result.get() == accept){
            gameController.sendDrawOffer(true);
            logger.info("Sending draw accept");
        }
    }
}
