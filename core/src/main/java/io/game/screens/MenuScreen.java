package io.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MenuScreen implements Screen {

    private Stage stage;

    private Music menuMusic;

    private Image titulo;
    private Image fondo;

    private ImageButton btnPlay;
    private ImageButton btnLoadGame;
    private ImageButton btnOptions;
    private ImageButton btnExit;

    public MenuScreen(SpriteBatch batch) {
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        cargarFondo();
        cargarTitulo();
        cargarBotones();
        cargarMusica();
    }

    private void cargarFondo() {
        Texture fondoTexture = new Texture(Gdx.files.internal("graphics/menu/fondo_menu.png"));
        fondo = new Image(fondoTexture);
        fondo.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(fondo);
    }

    private void cargarTitulo() {
        Texture tituloTexture = new Texture(Gdx.files.internal("graphics/menu/title.png"));
        titulo = new Image(tituloTexture);

        // Escalar proporcional al ancho de la pantalla
        float tituloWidth = Gdx.graphics.getWidth() * 0.6f;
        float aspectRatio = (float) tituloTexture.getHeight() / (float) tituloTexture.getWidth();
        float tituloHeight = tituloWidth * aspectRatio;

        titulo.setSize(tituloWidth, tituloHeight);

        // Pegado al top y centrado horizontalmente
        titulo.setPosition(
                (Gdx.graphics.getWidth() - tituloWidth) / 2f,
                Gdx.graphics.getHeight() - tituloHeight
        );

        stage.addActor(titulo);
    }

    private void cargarBotones() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        float buttonWidth = screenW * 0.3f;
        float buttonHeight = screenH * 0.15f;

        // Botones centrados verticalmente entre el título y el bottom
        float yTop = titulo.getY() - 20;
        float yBottom = 20;
        int numButtons = 4;
        float spacing = (yTop - yBottom) / (numButtons + 1);

        float centerX = (screenW - buttonWidth) / 2f;

        btnPlay = crearBoton("play", centerX, yTop - spacing, buttonWidth, buttonHeight, () -> System.out.println("PLAY"));
        btnLoadGame = crearBoton("loadgame", centerX, yTop - spacing * 2, buttonWidth, buttonHeight, () -> System.out.println("LOAD GAME"));
        btnOptions = crearBoton("options", centerX, yTop - spacing * 3, buttonWidth, buttonHeight, () -> System.out.println("OPTIONS"));
        btnExit = crearBoton("exit", centerX, yTop - spacing * 4, buttonWidth, buttonHeight, Gdx.app::exit);

        stage.addActor(btnPlay);
        stage.addActor(btnLoadGame);
        stage.addActor(btnOptions);
        stage.addActor(btnExit);
    }

    private ImageButton crearBoton(String name, float x, float y, float w, float h, Runnable accion) {
        Texture normal = new Texture(Gdx.files.internal("graphics/menu/" + name + "_normal.png"));
        Texture hover  = new Texture(Gdx.files.internal("graphics/menu/" + name + "_hover.png"));
        Texture click  = new Texture(Gdx.files.internal("graphics/menu/" + name + "_click.png"));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(normal);
        style.over = new TextureRegionDrawable(hover);
        style.down = new TextureRegionDrawable(click);

        ImageButton button = new ImageButton(style);
        button.setSize(w, h);
        button.setPosition(x, y);

        button.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float px, float py, int pointer, int button) {
                accion.run();
                return true;
            }
        });

        return button;
    }

    private void cargarMusica() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/S1.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.5f);
        menuMusic.play();
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (menuMusic != null) menuMusic.dispose();
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
