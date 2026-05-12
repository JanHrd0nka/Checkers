package cz.vse.java.checkers.common;

import java.util.ArrayList;
import java.util.List;

public class Game implements IGame {
    protected List<List<Figure>> figures;
    protected Boolean mustTake;
    public Game(Boolean mustTake)
    {
        this.mustTake = mustTake;
        setPieces();
    }

    @Override
    public Figure getPiece(Pos pos) {

        return null;
    }

    public Figure getPiece(int row, int col) {
        return figures.get(row).get(col);
    }

    @Override
    public List<Pos> getPossibleMoves(Pos pos, Figure figure) {

        List<Pos> possibleMoves = new ArrayList<>();

        int[][] directions = getDirectionsForFigure(figure);

        for (int[] dir : directions) {
            int newRow = pos.x() + dir[0];
            int newCol = pos.y() + dir[1];

            if (isValidPosition(newRow, newCol) &&
                    getPiece(newRow, newCol) == Figure.NONE) {
                possibleMoves.add(new Pos(newRow, newCol));
            }
        }

        findCaptureMoves(pos, figure, possibleMoves);

        return possibleMoves;
    }

    /**
     * Najde všechny pohyby, kdy se sebere figurka protivníka
     */
    private void findCaptureMoves(Pos pos, Figure figure, List<Pos> moves) {
        int[][] captureDirections = getCaptureDirections(figure);

        for (int[] dir : captureDirections) {
            int middleRow = pos.x() + dir[0];
            int middleCol = pos.y() + dir[1];

            int targetRow = pos.x() + dir[0] * 2;
            int targetCol = pos.y() + dir[1] * 2;

            // Kontrola, zda je mezi figurkou a cílem protivníková figurka
            if (isValidPosition(middleRow, middleCol) &&
                    isValidPosition(targetRow, targetCol) &&
                    isOpponentFigure(figure, getPiece(middleRow, middleCol)) &&
                    getPiece(targetRow, targetCol) == Figure.NONE) {

                moves.add(new Pos(targetRow, targetCol));
            }
        }
    }

    /**
     * Kontrola, zda je na pozici figurka protivníka
     */
    private boolean isOpponentFigure(Figure myFigure, Figure otherFigure) {
        boolean isWhite = myFigure == Figure.WHITE_MAN || myFigure == Figure.WHITE_KING;
        boolean otherIsWhite = otherFigure == Figure.WHITE_MAN || otherFigure == Figure.WHITE_KING;

        return otherFigure != Figure.NONE && isWhite != otherIsWhite;
    }




    @Override
    public void setPiece(Pos pos, Figure figure) {

    }

    public void setPiece(int row, int col, Figure figure) {
        figures.get(row).set(col, figure);
    }

    public void movePiece(int row_from, int col_from, int row_to, int col_to){
        Figure figure = getPiece(row_from, col_from);
        List<Pos> possilbeMoves = getPossibleMoves(new Pos(row_from, col_from), figure);
        List<Pos> capturedFigures = new ArrayList<>();
        if(!possilbeMoves.isEmpty()){
            if(possilbeMoves.contains(new Pos(row_to, col_to))){
                setPiece(row_from, col_from, Figure.NONE);
                setPiece(row_to, col_to, figure);
            }

            // Zjistit, zda je to krocení
            int rowDiff = Math.abs(row_to - row_from);
            int colDiff = Math.abs(col_to - col_from);

            if (rowDiff == 2 && colDiff == 2) {
                // Je to krocení - sebrat figurku
                int captureRow = row_from + (row_to - row_from) / 2;
                int captureCol = col_from + (col_to - col_from) / 2;

                setPiece(captureRow, captureCol, Figure.NONE);
                capturedFigures.add(new Pos(captureRow, captureCol));
            }
        }

    }

    public void setPieces() {
        figures = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            List<Figure> row = new ArrayList<>();

            for (int j = 0; j < 8; j++) {

                if ((i + j) % 2 == 0) {
                    row.add(Figure.NONE);
                }
                else if (i < 3) {
                    row.add(Figure.BLACK_MAN);
                }
                else if (i > 4) {
                    row.add(Figure.WHITE_MAN);
                }
                else {
                    row.add(Figure.NONE);
                }
            }

            figures.add(row);
        }
    }

    public List<List<Figure>> getFigures() {
        return figures;
    }

    /**
     * Vrátí směry pohybu pro konkrétní figurku
     * Normální figurky se pohybují jen dopředu, králové všemi směry
     */
    private int[][] getDirectionsForFigure(Figure figure) {
        if (figure == Figure.WHITE_KING || figure == Figure.BLACK_KING) {
            return new int[][] {
                    {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
            };
        }

        if (figure == Figure.WHITE_MAN) {
            return new int[][] {{-1, -1}, {-1, 1}};  // dopředu
        }

        if (figure == Figure.BLACK_MAN) {
            return new int[][] {{1, -1}, {1, 1}};    // dopředu
        }

        return new int[][] {};
    }

    /**
     * Vrátí všechny diagonální směry (pro krocení)
     */
    private int[][] getCaptureDirections(Figure figure) {
        return new int[][] {
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
    }

    /**
     * Kontrola, zda je pozice na desce
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

}
