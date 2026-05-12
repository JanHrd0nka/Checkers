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
    List<Pos> capturedFigures = new ArrayList<>();

    @Override
    public Figure getPiece(Pos pos) { return figures.get(pos.x()).get(pos.y()); }

    public Figure getPiece(int row, int col) {
        return figures.get(row).get(col);
    }

    @Override
    public List<Pos> getPossibleMoves(Pos pos, Figure figure) {

        List<Pos> possibleMoves = new ArrayList<>();

        findPossibleMoves(pos, figure, possibleMoves);

        findCaptureMoves(pos, figure, possibleMoves);

        return possibleMoves;
    }

    /**
     * Najde všechny pohyby, kdy se nebere figurka protivníka
     * @param pos aktuální pozice vybrané figurky
     * @param figure typ aktuální vybrané figurky
     * @param possibleMoves kolekce možných pohybů
     */
    private void findPossibleMoves(Pos pos, Figure figure, List<Pos> possibleMoves) {
        int[][] directions = getDirectionsForFigure(figure);

        for (int[] dir : directions) {
            int newRow = pos.x() + dir[0];
            int newCol = pos.y() + dir[1];

            if (isValidPosition(newRow, newCol) &&
                    getPiece(newRow, newCol) == Figure.NONE) {
                possibleMoves.add(new Pos(newRow, newCol));
            }
        }
    }

    /**
     * Najde všechny pohyby, kdy se sebere figurka protivníka
     * @param pos aktuální pozice vybrané figurky
     * @param figure typ aktuální vybrané figurky
     * @param moves kolekce možných pohybů
     */
    private void findCaptureMoves(Pos pos, Figure figure, List<Pos> moves) {
        int[][] captureDirections = getDirectionsForFigure(figure);

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
        figures.get(pos.x()).set(pos.y(), figure);
    }

    public void setPiece(int row, int col, Figure figure) {
        figures.get(row).set(col, figure);
    }

    public void movePiece(Pos from, Pos to){
        Figure figure = getPiece(from.x(), from.y());
        List<Pos> possilbeMoves = getPossibleMoves(new Pos(from.x(), from.y()), figure);

        if(!possilbeMoves.isEmpty()){
            if(possilbeMoves.contains(new Pos(to.x(), to.y()))){
                setPiece(from.x(), from.y(), Figure.NONE);


                // Zjistit, zda je to krocení
                int rowDiff = Math.abs(to.x() - from.x());
                int colDiff = Math.abs(to.y() - from.y());

                if (rowDiff == 2 && colDiff == 2) {
                    // Je to krocení - sebrat figurku
                    int captureRow = from.x() + (to.x() - from.x()) / 2;
                    int captureCol = from.y() + (to.y() - from.y()) / 2;

                    setPiece(captureRow, captureCol, Figure.NONE);
                    capturedFigures.add(new Pos(captureRow, captureCol));
                }

                if(figure == Figure.WHITE_MAN){
                    if(to.x() == 0){
                        //changeToKing(figure); --budoucí úprava---
                        figure = Figure.WHITE_KING;
                    }
                } else if (figure == Figure.BLACK_MAN) {
                    if(to.x() == 7){
                        figure = Figure.BLACK_KING;
                    }
                }


                setPiece(to.x(), to.y(), figure);
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
     * Kontrola, zda je pozice na desce
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }


    private void changeToKing(Figure figure) {
        //pro budoucí potřeby
    }

}
