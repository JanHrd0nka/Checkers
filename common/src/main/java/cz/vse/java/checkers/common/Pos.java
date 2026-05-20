package cz.vse.java.checkers.common;

import static java.lang.Math.abs;
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

    public boolean isCaptureMove(Pos other){
        return abs(this.x - other.x()) == 2;
    }
}
