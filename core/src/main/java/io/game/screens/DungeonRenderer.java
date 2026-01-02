package io.game.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import io.game.managers.Resources;
import io.game.maps.Room;

public class DungeonRenderer {

    private float tileW, tileH;
    private Texture bg;
    private Texture stairsTex;

    public DungeonRenderer(float tileW, float tileH) {
        this.tileW = tileW;
        this.tileH = tileH;
        
        bg = Resources.getTexture("background", "graphics/tilesets/dungeons_tilesets");
        stairsTex = Resources.getTexture("down_stairs", "graphics/sprites/world_objects");
    }

    public void render(SpriteBatch batch, Iterable<Room> rooms) {

        // ===== 1. Dibujar fondo infinito =====
        if (bg != null) {
            float bgSize = tileW;
            for (float x = -5000; x <= 5000; x += bgSize) {
                for (float y = -5000; y <= 5000; y += bgSize) {
                    batch.draw(bg, x, y, bgSize, bgSize);
                }
            }
        }

        // ===== 2. Dibujar habitaciones =====
        for (Room r : rooms) {

            float px = r.x * tileW;
            float py = r.y * tileH;

            TextureRegion reg = r.getRegion();
            if (reg != null) {
                batch.draw(reg, px, py, tileW, tileH);
            } else {
                // fallback se podría dibujar rectángulo o nada
            }

            // ===== 3. Dibujar escalera si la hay =====
            if (r.hasStairs && stairsTex != null) {
                // Escalar las escaleras para que ocupen un espacio apropiado en el centro de la habitación
                float stairSize = tileW / 4f; // Escaleras más grandes y visibles
                float cx = px + tileW * 0.5f - stairSize * 0.5f;
                float cy = py + tileH * 0.5f - stairSize * 0.5f;
                batch.setColor(Color.WHITE);
                batch.draw(stairsTex, cx, cy, stairSize, stairSize);
            }
        }
    }
    public void dispose() {
        bg = null;
            stairsTex = null;
    }
}
