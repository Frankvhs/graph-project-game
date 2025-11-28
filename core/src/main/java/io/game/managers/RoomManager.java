package io.game.managers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.game.components.RoomTemplate;

public class RoomManager {

    public static final int SPRITE_W = 160;
    public static final int SPRITE_H = 160;

    public static void load() {

        Resources.loadTexture("room_sheet", "graphics/rooms");
        Resources.finish();

        Texture sheet = Resources.getTexture("room_sheet", "graphics/rooms");

        for (RoomTemplate t : RoomTemplate.values()) {
            int index = t.getSpriteIndex();

            TextureRegion reg = new TextureRegion(
                sheet,
                index * SPRITE_W,
                0,
                SPRITE_W,
                SPRITE_H
            );

            t.setRegion(reg);
        }
    }
}
