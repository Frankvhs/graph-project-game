package io.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.game.screens.MenuScreen;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private MenuScreen menu;

    @Override
    public void create() {
        batch = new SpriteBatch();
        menu = new MenuScreen(batch);
    }

    @Override
    public void render() {
        menu.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
        menu.dispose();
        batch.dispose();
    }
}
