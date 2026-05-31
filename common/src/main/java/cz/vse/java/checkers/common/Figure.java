package cz.vse.java.checkers.common;
/**
 * Represents a piece on the checkers board.
 *
 * Used to encode the state of each board cell.
 *
 * Values:
 * - NONE: empty field
 * - WHITE_MAN: regular white piece
 * - WHITE_KING: promoted white piece (king)
 * - BLACK_MAN: regular black piece
 * - BLACK_KING: promoted black piece (king)
 *
 * @author Jan Hrdonka
 * @version 1.0
 */
public enum Figure
{
    NONE,
    WHITE_MAN,
    WHITE_KING,
    BLACK_MAN,
    BLACK_KING
}
