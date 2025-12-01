package io.game.components;

public enum Direction {
    N(0, 1),
    E(1, 0),
    S(0, -1),
    O(-1, 0);

    public final int dx;
    public final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Direction opposite() {
        switch (this) {
            case N: return S;
            case S: return N;
            case E: return O;
            case O: return E;
        }
        return N;
    }
}
