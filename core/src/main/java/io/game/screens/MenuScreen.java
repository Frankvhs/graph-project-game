package io.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.game.GameMain;
import io.game.managers.Resources;
import io.game.ui.Button;

public class MenuScreen implements Screen {
	public static final String BASE_PATH = "graphics/ui/menu/main";

	private Stage stage;
	private GameMain game;
	private Music menuMusic;
	private Sound buttonSound;
	private Image background;

	// Utils
	private Texture getTexture(String name) {
		return Resources.getTexture(name, BASE_PATH);
	}

	public MenuScreen(GameMain game) {
		this.game = game;
		stage = new Stage(new ScreenViewport(), game.batch);
		Gdx.input.setInputProcessor(stage);

		loadAssets();
		loadSounds();
		loadMusic();

		createBackground();
		createMenu();
	}

	private void loadAssets() {
		//
		Button.loadDrawable("play", BASE_PATH);
		Button.loadDrawable("options", BASE_PATH);
		Button.loadDrawable("loadgame", BASE_PATH);
		Button.loadDrawable("exit", BASE_PATH);

		//
		Resources.loadTexture("fondo_menu", BASE_PATH);
		Resources.loadTexture("title", BASE_PATH);

		Resources.finish();
	}

	private void createBackground() {
		background = new Image(getTexture("fondo_menu"));
		background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		stage.addActor(background);
	}

	private void createMenu() {
		float screenW = Gdx.graphics.getWidth();
		float screenH = Gdx.graphics.getHeight();

		Table table = new Table();
		table.setFillParent(true);
		table.center();
		stage.addActor(table);

		//
		// title
		//
		Texture titleTex = getTexture("title");
		Image title = new Image(titleTex);
		float targetWidth = screenW * 0.6f;
		float targetHeight = targetWidth * titleTex.getHeight() / titleTex.getWidth();
		table.add(title).width(targetWidth).height(targetHeight).row();

		//
		// buttons
		//
		float buttonWidth = screenW * 0.28f;
		float buttonHeight = screenH * 0.17f;
		table.add(new Button("play", BASE_PATH, () -> {
			System.out.println("PLAY");
			game.setScreen(game.gameScreen);
		}, buttonSound)).width(buttonWidth).height(buttonHeight).row();

		table.add(new Button("loadgame", BASE_PATH, () -> System.out.println("LOAD GAME"), buttonSound))
				.width(buttonWidth).height(buttonHeight).row();

		table.add(new Button("options", BASE_PATH, () -> System.out.println("OPTIONS"), buttonSound)).width(buttonWidth)
				.height(buttonHeight).row();

		table.add(new Button("exit", BASE_PATH, Gdx.app::exit, buttonSound)).width(buttonWidth).height(buttonHeight)
				.row();

	}

	private void loadMusic() {
		menuMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/Music/S1.mp3"));
		menuMusic.setLooping(true);
		menuMusic.setVolume(0.5f);
		menuMusic.play();
	}

	private void loadSounds() {
		buttonSound = Gdx.audio.newSound(Gdx.files.internal("audio/Fx/Sbottom.mp3"));
	}

	// SCREEN METHODS
	@Override
	public void render(float delta) {
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void dispose() {
		stage.dispose();
		if (menuMusic != null)
			menuMusic.dispose();
		if (buttonSound != null)
			buttonSound.dispose();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void resize(int w, int h) {
		background.setSize(w, h);
		stage.getViewport().update(w, h, true);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}
}
