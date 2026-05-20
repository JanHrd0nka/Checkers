package cz.vse.java.checkers.common;

import java.util.List;

public interface IGame
{
    Figure getPiece(Pos pos);
    List<List<Pos>> getPossibleMoves(Pos pos);
    Figure setPiece(Pos pos, Figure figure);
}
