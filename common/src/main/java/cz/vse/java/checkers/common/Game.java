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
    public List<Pos> getPossibleMoves(Pos pos) {
        return null;
    }

    @Override
    public void setPiece(Pos pos, Figure figure) {

    }

    public void setPiece(int row, int col, Figure figure) {
        figures.get(row).set(col, figure);
    }

    public void movePiece(int row_from, int col_from, int row_to, int col_to){
        Figure figure = getPiece(row_from, col_from);
        setPiece(row_from, col_from, Figure.NONE);
        setPiece(row_to, col_to, figure);
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
}
