package io.game.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

import io.game.GameMain;
import io.game.entities.characters.Player;

public class GameScreen implements Screen {
	private Stage stage = new Stage();
	private SpriteBatch batch;
	private GameMain game;
	private Player player;
	
	public GameScreen(GameMain game) {
		this.game = game;
		this.batch = game.batch;
		Player.loadTextures();
		player = new Player();
		
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(Color.BLACK);
		
		player.update(delta);
		batch.begin();
		
		player.render(batch);
		
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
