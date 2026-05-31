package cz.vse.java.checkers.common;

import static java.lang.Math.abs;

/**
 * Represents a position on the checkers board.
 * <p>
 * This immutable record is used throughout the game logic
 * to represent coordinates of pieces and moves.
 *
 * @author Jan Hrdonka
 * @version 1.0
 */
public record Pos(int x, int y) {
    public Pos add(Pos other) {
        return new Pos(
                this.x + other.x,
                this.y + other.y
        );
    }

    public Pos subtract(Pos other) {
        return new Pos(
                this.x - other.x,
                this.y - other.y
        );
    }

    public Pos divide(int k) {
        return new Pos(
                this.x / k,
                this.y / k
        );
    }

    public boolean isCaptureMove(Pos other) {
        return abs(this.x - other.x()) == 2;
    }
}
