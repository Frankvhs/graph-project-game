package io.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.game.objects.Button;

public class MenuScreen implements Screen {

    private SpriteBatch batch;
    private Texture fondoTexture;
    private Sprite fondo;

    // Música
    private Music menuMusic;

    // Botones
    private Button btnPlay;
    private Button btnLoadGame;
    private Button btnOptions;
    private Button btnExit;

    public MenuScreen(SpriteBatch batch) {
        this.batch = batch;
        cargarFondo();
        cargarBotones();
        cargarMusica(); 
    }

    private void cargarFondo() {
        fondoTexture = new Texture(Gdx.files.internal("graphics/menu/menu_principal.png"));
        fondo = new Sprite(fondoTexture);
        fondo.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void cargarBotones() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // Tamaño proporcional de los botones
        float buttonWidth = screenW * 0.3f;
        float buttonHeight = screenH * 0.12f;

        // Posición inicial del primer botón
        float startY = screenH * 0.5f;  // ajustado para que no tape el título
        float centerX = (screenW - buttonWidth) / 2f;
        float spacing = buttonHeight * 1.1f;

        // Se pasa la ruta base a cada botón
        btnPlay     = new Button("play", centerX, startY, buttonWidth, buttonHeight, "graphics/menu/");
        btnLoadGame = new Button("loadgame", centerX, startY - spacing, buttonWidth, buttonHeight, "graphics/menu/");
        btnOptions  = new Button("options", centerX, startY - spacing * 2, buttonWidth, buttonHeight, "graphics/menu/");
        btnExit     = new Button("exit", centerX, startY - spacing * 3, buttonWidth, buttonHeight, "graphics/menu/");
    }

    // Cargar música del menú
    private void cargarMusica() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/S1.mp3"));
        menuMusic.setLooping(true);   // Para que se repita indefinidamente
        menuMusic.setVolume(0.5f);    // Ajusta el volumen si quieres
        menuMusic.play();
    }

    @Override
    public void render(float delta) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Actualizar animaciones de los botones
        btnPlay.update(mx, my);
        btnLoadGame.update(mx, my);
        btnOptions.update(mx, my);
        btnExit.update(mx, my);

        // Manejo de clics
        if (Gdx.input.justTouched()) {
            if (btnPlay.isHover(mx, my)) {
                btnPlay.click();
                irAPantallaJuego();
            } else if (btnLoadGame.isHover(mx, my)) {
                btnLoadGame.click();
                irALoadGame();
            } else if (btnOptions.isHover(mx, my)) {
                btnOptions.click();
                irAOptions();
            } else if (btnExit.isHover(mx, my)) {
                btnExit.click();
                Gdx.app.exit();
            }
        }

        batch.begin();
        fondo.draw(batch);

        btnPlay.draw(batch);
        btnLoadGame.draw(batch);
        btnOptions.draw(batch);
        btnExit.draw(batch);
        batch.end();
    }

    // Funciones para cada pantalla (actualmente prints, se cambiarán por setScreen)
    private void irAPantallaJuego() {
        System.out.println("PLAY: ir a GameScreen");
    }

    private void irALoadGame() {
        System.out.println("LOAD GAME: ir a LoadGameScreen");
    }

    private void irAOptions() {
        System.out.println("OPTIONS: ir a OptionsScreen");
    }

    @Override
    public void dispose() {
        fondoTexture.dispose();
        btnPlay.dispose();
        btnLoadGame.dispose();
        btnOptions.dispose();
        btnExit.dispose();
        if (menuMusic != null) menuMusic.dispose(); 
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
