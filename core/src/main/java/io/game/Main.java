package io.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.game.screens.MenuScreen;

public class Main extends Game {

    private SpriteBatch batch;
    private MenuScreen menuScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        
        // pantallas
        menuScreen = new MenuScreen(batch);
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
