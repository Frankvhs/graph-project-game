package io.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.game.managers.Resources;
import io.game.screens.GameScreen;
import io.game.screens.MenuScreen;

public class GameMain extends Game {

    public SpriteBatch batch;
    public MenuScreen menuScreen;
    public GameScreen gameScreen;
    public boolean needGameReset = false;

    @Override
    public void create() {
        batch = new SpriteBatch();

        Resources.finish();
        
        // Crear pantallas
        menuScreen = new MenuScreen(this);
        gameScreen = new GameScreen(this);
        
        setScreen(menuScreen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (menuScreen != null) {
            menuScreen.dispose();
        }
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        Resources.dispose();
    }
}