package io.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.game.screens.GameScreen;
import io.game.screens.MenuScreen;

public class GameMain extends Game {

    public SpriteBatch batch;
    public MenuScreen menuScreen;
    public GameScreen gameScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        
        // pantallas
        menuScreen = new MenuScreen(this);
        gameScreen = new GameScreen(batch);
        setScreen(menuScreen);
    }

    @Override
    public void render() {
        screen.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
        menuScreen.dispose();
        batch.dispose();
    }
}
