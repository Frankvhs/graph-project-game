package io.game.maps;

import io.game.components.RoomTemplate;
import io.game.components.Direction;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.EnumMap;

public class Room {

    public final int x, y;
    private RoomTemplate template; 

    public boolean isStart = false;
    public boolean hasStairs = false;
    public boolean discovered = false;

    private EnumMap<Direction, Boolean> connected = new EnumMap<>(Direction.class);

    public Room(int x, int y, RoomTemplate template) {
        this.x = x;
        this.y = y;
        this.template = template;
        for (Direction d : Direction.values()) connected.put(d, false);
    }

    public void setTemplate(RoomTemplate tpl) {
        this.template = tpl;
    }

    public RoomTemplate getTemplate() {
        return template;
    }

    public void connect(Direction d) {
        connected.put(d, true);
    }

    public boolean isConnected(Direction d) {
        Boolean b = connected.get(d);
        return b != null && b;
    }

    public boolean hasDoor(Direction d) {
        return template.hasDoor(d);
    }

    public TextureRegion getRegion() {
        return template.getRegion();
    }

    public void clearConnectionsExceptNothing() {
        for (Direction d : Direction.values()) {
            connected.put(d, false); // o como manejes tu sistema de conexiones
        }
    }

    public Direction getSingleDirection() {
        // Si ya es SINGLE, devuelve su única puerta
        if (this.getTemplate().getDoors().size() == 1) {
            return this.getTemplate().getDoors().iterator().next();
        }
        // Si tiene 0 puertas, devolver algo consistente (por ejemplo norte)
        return Direction.N;
    }




    public float centerRoomX() { return x + 0.5f; }
    public float centerRoomY() { return y + 0.5f; }
}
