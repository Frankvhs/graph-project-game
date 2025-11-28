package io.game.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.EnumSet;
import java.util.Set;

public enum RoomTemplate {

    N_SINGLE(EnumSet.of(Direction.N), 0),
    E_SINGLE(EnumSet.of(Direction.E), 1),
    S_SINGLE(EnumSet.of(Direction.S), 2),
    O_SINGLE(EnumSet.of(Direction.O), 3),

    NE(EnumSet.of(Direction.N, Direction.E), 4),
    NS(EnumSet.of(Direction.N, Direction.S), 5),
    NO(EnumSet.of(Direction.N, Direction.O), 6),

    ES(EnumSet.of(Direction.E, Direction.S), 7),
    EO(EnumSet.of(Direction.E, Direction.O), 8),
    SO(EnumSet.of(Direction.S, Direction.O), 9),

    NES(EnumSet.of(Direction.N, Direction.E, Direction.S), 10),
    NEO(EnumSet.of(Direction.N, Direction.E, Direction.O), 11),
    ESO(EnumSet.of(Direction.E, Direction.S, Direction.O), 12),
    SON(EnumSet.of(Direction.S, Direction.O, Direction.N), 13),

    NESO(EnumSet.of(Direction.N, Direction.E, Direction.S, Direction.O), 14);

    private final Set<Direction> doors;
    private final int spriteIndex;
    private TextureRegion region;

    RoomTemplate(Set<Direction> doors, int index) {
        this.doors = doors;
        this.spriteIndex = index;
    }

    public Set<Direction> getDoors() {
        return doors;
    }

    public int getSpriteIndex() {
        return spriteIndex;
    }

    public void setRegion(TextureRegion r) {
        this.region = r;
    }

    public TextureRegion getRegion() {
        return region;
    }

    public boolean hasDoor(Direction d) {
        return doors.contains(d);
    }

    public int doorCount() {
        return doors.size();
    }
}
