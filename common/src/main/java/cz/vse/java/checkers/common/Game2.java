package cz.vse.java.checkers.common;

import java.util.ArrayList;
import java.util.List;

public class Game2 implements IGame {
    private List<String> gameHistory;
    private List<List<Figure>> board;
    private boolean whiteToMove;
    private static final int BOARD_SIZE = 8;
    private final boolean mustTake;
    public Game2(boolean mustTake){
        setPieces();
        whiteToMove = true;
        this.mustTake = mustTake;
        gameHistory = new ArrayList<>();
        gameHistory.add(toContent());
    }
    public Game2(String content){
        gameHistory = new ArrayList<>();
        whiteToMove = true;
        mustTake = false;
        board = new ArrayList<>(BOARD_SIZE);
        for (int i = 0; i < BOARD_SIZE; ++i){
            board.add(new ArrayList<>(BOARD_SIZE));
            for (int j = 0; j < BOARD_SIZE; ++j){
                board.get(i).add(Figure.NONE);
            }
        }
        if (content.length() == 64){
            for (int i = 0; i < 64; ++i){
                int x = i % 8;
                int y = 7 - (i / 8);
                char charFigure = content.charAt(i);
                Figure figure;
                switch (charFigure) {
                    case '1' -> figure = Figure.WHITE_MAN;
                    case '2' -> figure = Figure.WHITE_KING;
                    case '3' -> figure = Figure.BLACK_MAN;
                    case '4' -> figure = Figure.BLACK_KING;
                    default -> figure = Figure.NONE;
                }
                setPiece(new Pos(x, y), figure);
            }
        }
    }
    public String toContent() {

        StringBuilder sb = new StringBuilder(64);

        for (int i = 0; i < 64; ++i) {

            int x = i % 8;
            int y = 7 - (i / 8);

            Figure figure = getPiece(new Pos(x, y));

            char c = switch (figure) {
                case NONE -> '0';
                case WHITE_MAN -> '1';
                case WHITE_KING -> '2';
                case BLACK_MAN -> '3';
                case BLACK_KING -> '4';
            };

            sb.append(c);
        }

        return sb.toString();
    }

    public boolean updateBoard(String state){
        if(state.length() == 64) {
            for (int i = 0; i < 64; ++i) {
                int x = i % 8;
                int y = 7 - (i / 8);
                char charFigure = state.charAt(i);
                Figure figure;
                switch (charFigure) {
                    case '1' -> figure = Figure.WHITE_MAN;
                    case '2' -> figure = Figure.WHITE_KING;
                    case '3' -> figure = Figure.BLACK_MAN;
                    case '4' -> figure = Figure.BLACK_KING;
                    default -> figure = Figure.NONE;
                }
                setPiece(new Pos(x, y), figure);
            }
            whiteToMove = !whiteToMove;
            return true;
        }else{
            return false;
        }
    }


    public boolean makeMove(List<Pos> path){
        boolean result = false;
        if (path.size() >= 2 && (getPossibleMoves(path.getFirst()).contains(path))) {
            Pos from = path.getFirst();
            var figure = setPiece(from, Figure.NONE);
            if (from.isCaptureMove(path.get(1))) {
                for (int i = 1; i < path.size(); ++i){
                    Pos direction = path.get(i).subtract(path.get(i - 1));
                    Pos normalizedDir = direction.divide(2);
                    setPiece(path.get(i - 1).add(normalizedDir), Figure.NONE);
                }
            }
            setPiece(path.getLast(), figure);
            checkPromote(path.getLast());
            whiteToMove = !whiteToMove;
            gameHistory.add(toContent());
            result = true;
            }
        return result;
    }

    @Override
    public Figure getPiece(Pos pos) {
        Figure result = Figure.NONE;
        if (pos.x() >= 0 && pos.x() < BOARD_SIZE && pos.y() >= 0 && pos.y() < BOARD_SIZE){
            result = board.get(pos.x()).get(pos.y());
        }
        return result;
    }

    @Override
    public List<List<Pos>> getPossibleMoves(Pos pos) {
        List<List<Pos>> result = new ArrayList<>();
        if (validateColor(pos)){
            List<Pos> directions = getDirections(pos);
            for (Pos direction : directions){
                exploreDirection(new ArrayList<>(List.of(pos)), direction, directions, result, new ArrayList<>());
            }
            //trimPossibleMoves(result);
        }
        return result;
    }

    private void trimPossibleMoves(List<List<Pos>> moves){
        moves.removeIf(move -> move.size() <= 1);
        if (requireCaptureMove()){
            moves.removeIf(move -> !move.getFirst().isCaptureMove(move.get(1)));
        }
        for (List<Pos> move : moves) {
            move.removeFirst();
        }
    }

    private void exploreDirection(List<Pos> path, Pos direction, List<Pos> possibleDirections, List<List<Pos>> out, List<Pos> capturedPieces){
        Pos newPos = path.getLast().add(direction);
        if(isValidMove(newPos)){
            if ((getPiece(newPos) == Figure.NONE || capturedPieces.contains(newPos))){
                if (path.size() == 1)
                {
                    path.add(newPos);
                    out.add(path);
                }
            }
            else if (isOppositeColor(newPos) && !capturedPieces.contains(newPos)){
                Pos newPos2 = newPos.add(direction);
                if (getPiece(newPos2) == Figure.NONE || capturedPieces.contains(newPos2)){
                    capturedPieces.add(newPos);
                    path.add(newPos2);
                    out.add(path);
                    // explore other options
                    for (Pos possibleDirection : possibleDirections){
                        if (!newPos2.add(possibleDirection).equals(newPos)){
                            exploreDirection(new ArrayList<>(path),
                                                possibleDirection,
                                                possibleDirections,
                                                out,
                                                new ArrayList<>(capturedPieces));
                        }
                    }
                }
            }
        }
    }

    private List<Pos> getDirections(Pos pos){
        List<Pos> result = new ArrayList<>();
        if (getPiece(pos) == Figure.BLACK_KING || getPiece(pos) == Figure.WHITE_KING){
            result.add(new Pos(-1, -1));
            result.add(new Pos(1, -1));
            result.add(new Pos(-1, 1));
            result.add(new Pos(1, 1));
        }
        else if (getPiece(pos) == Figure.WHITE_MAN){
            result.add(new Pos(-1, 1));
            result.add(new Pos(-1, -1));
        }
        else if (getPiece(pos) == Figure.BLACK_MAN){
            result.add(new Pos(1, 1));
            result.add(new Pos(1, -1));
        }
        return result;
    }

    private boolean isOppositeColor(Pos pos){
        return !validateColor(pos);
    }

    @Override
    public Figure setPiece(Pos pos, Figure figure) {
        return board.get(pos.x()).set(pos.y(), figure);
    }

    private void setPieces() {
        board = new ArrayList<>(BOARD_SIZE);
        for (int i = 0; i < BOARD_SIZE; i++) {
            List<Figure> row = new ArrayList<>(BOARD_SIZE);
            for (int j = 0; j < BOARD_SIZE; j++) {
                if ((i + j) % 2 == 0) {
                    row.add(Figure.NONE);
                } else if (i < 3) {
                    row.add(Figure.BLACK_MAN);
                } else if (i > 4) {
                    row.add(Figure.WHITE_MAN);
                } else {
                    row.add(Figure.NONE);
                }
            }
            board.add(row);
        }
    }

    private boolean validateColor(Pos pos){
        if (whiteToMove){
            return getPiece(pos) == Figure.WHITE_MAN || getPiece(pos) == Figure.WHITE_KING;
        }
        else{
            return getPiece(pos) == Figure.BLACK_MAN || getPiece(pos) == Figure.BLACK_KING;
        }
    }

    private void checkPromote(Pos pos){
        if (pos.x() == 0 && getPiece(pos) == Figure.WHITE_MAN){
            setPiece(pos, Figure.WHITE_KING);
        }
        else if ((pos.x() == BOARD_SIZE - 1) && getPiece(pos) == Figure.BLACK_MAN){
            setPiece(pos, Figure.BLACK_KING);
        }
    }
    public boolean checkGameState(){
        boolean result = false;
        for (int i = 0; i < BOARD_SIZE; ++i){
            for (int j = 0; j < BOARD_SIZE; ++j){
                if (!getPossibleMoves(new Pos(i, j)).isEmpty()){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    public String getHistory(int index){
        String result = new String();
        if (index < gameHistory.size())
        {
            result = gameHistory.get(index);
        }
        return result;
    }
    private boolean requireCaptureMove(){
        boolean result = mustTake;
        if (result){
            for (int i = 0; i < BOARD_SIZE; ++i){
                for (int j = 0; j < BOARD_SIZE; ++j) {
                    Pos pos = new Pos(i, j);
                    var moves = getPossibleMoves(pos);
                    for (var move : moves){
                        if (move.size() > 1 || move.getFirst().isCaptureMove(pos)){
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean isValidMove(Pos pos){
        boolean row_result = pos.x() >= 0 && pos.x() < BOARD_SIZE;
        boolean col_result = pos.y() >= 0 && pos.y() < BOARD_SIZE;
        return row_result && col_result;
    }


    public boolean isWhiteToMove(){
        return whiteToMove;
    }
}
