package io.game.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;

import io.game.maps.Room;

public class DungeonRenderer {

    private float tileW, tileH;
    private Texture bg;
    private Texture stairsTex;

    public DungeonRenderer(float tileW, float tileH) {
        this.tileW = tileW;
        this.tileH = tileH;

        try {
            bg = new Texture("graphics/tilesets/dungeons_tilesets/background.png");
        } catch (Exception e) { bg = null; }

        try {
            stairsTex = new Texture("graphics/tilesets/dungeons_tilesets/stairs.png");
        } catch (Exception e) { stairsTex = null; }
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
                // la habitación es 160x160 lógico (sprite). Las paredes 16px => espacio libre central = 128
                // escalera original 16px -> ratio = 160 / 16 = 10 -> stairSize = tileW / 10
                float stairSize = tileW / 10f;
                float cx = px + tileW * 0.5f - stairSize * 0.5f;
                float cy = py + tileH * 0.5f - stairSize * 0.5f;
                batch.setColor(Color.WHITE);
                batch.draw(stairsTex, cx, cy, stairSize, stairSize);
            }
        }
    }
}
