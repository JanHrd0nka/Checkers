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

    @Override
    public List<Pos> getPossibleMoves(Pos pos) {
        return null;
    }

    @Override
    public void setPiece(Pos pos, Figure figure) {

    }

    private void setPieces() {
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
}
