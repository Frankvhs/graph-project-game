package io.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.game.components.HealthComponent;
import io.game.managers.Resources;

public class HealthBar {
    public static final String BASE_PATH = "graphics/ui/playerUI";
    
    private Texture[] lifeBarTextures;
    private float x, y;
    private float width, height;
    
    public HealthBar(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        loadTextures();
    }
    
    private void loadTextures() {
        lifeBarTextures = new Texture[5];
        for (int i = 0; i < 5; i++) {
            Resources.loadTexture("LifeBar" + i, BASE_PATH);
        }
        Resources.finish();
        
        for (int i = 0; i < 5; i++) {
            lifeBarTextures[i] = Resources.getTexture("LifeBar" + i, BASE_PATH);
        }
    }
    
    public void render(SpriteBatch batch, HealthComponent health, int maxHealth) {
        // Calcular el índice de la barra basado en vida actual
        // Cada 5 puntos de vida = 1 nivel de barra
        // 20 vida = LifeBar4, 15 = LifeBar3, 10 = LifeBar2, 5 = LifeBar1, 0 = LifeBar0
        int currentHealth = health.getHealth();
        int textureIndex;
        
        if (currentHealth >= 20) {
            textureIndex = 4; // LifeBar4 - llena (20 vida)
        } else if (currentHealth >= 15) {
            textureIndex = 3; // LifeBar3 (15 vida)
        } else if (currentHealth >= 10) {
            textureIndex = 2; // LifeBar2 (10 vida)
        } else if (currentHealth >= 5) {
            textureIndex = 1; // LifeBar1 (5 vida)
        } else {
            textureIndex = 0; // LifeBar0 - vacía (0 vida)
        }
        
        batch.draw(lifeBarTextures[textureIndex], x, y, width, height);
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
