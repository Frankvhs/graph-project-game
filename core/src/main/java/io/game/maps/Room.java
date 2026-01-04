package io.game.maps;

import io.game.components.RoomTemplate;
import io.game.components.Direction;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.EnumMap;

/**
 * Room ahora permite cambiar template (setTemplate) para poder añadir puertas
 * dinámicamente cuando el generador decide crear ciclos.
 */
public class Room {

    public final int x, y;
    private RoomTemplate template; // ya no final

    public boolean isStart = false;
    public boolean hasStairs = false;
    public boolean hasChest = false;
    public boolean chestOpened = false;
    public boolean hasKey = false;
    public boolean keyCollected = false;
    public boolean discovered = false;

    private EnumMap<Direction, Boolean> connected = new EnumMap<>(Direction.class);

    public Room(int x, int y, RoomTemplate template) {
        this.x = x;
        this.y = y;
        this.template = template;
        for (Direction d : Direction.values()) connected.put(d, false);
    }

    /** Cambiar template (se usa para añadir puertas dinámicamente) */
    public void setTemplate(RoomTemplate tpl) {
        this.template = tpl;
    }

    public RoomTemplate getTemplate() {
        return template;
    }

    public void connect(Direction d) {
        connected.put(d, true);
    }
    
    public void disconnect(Direction d) {
        connected.put(d, false);
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

    public float centerRoomX() { return x + 0.5f; }
    public float centerRoomY() { return y + 0.5f; }
}
