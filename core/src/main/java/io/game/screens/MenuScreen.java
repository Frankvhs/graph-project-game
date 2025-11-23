package io.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.game.objects.Button;

public class MenuScreen implements Screen {

    private Stage stage;          
    private Music menuMusic;      
    private Sound buttonSound;    
    private Image titulo;         
    private Image fondo;          

    public MenuScreen(SpriteBatch batch) {
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage); 

        cargarFondo();      
        cargarTitulo();     
        cargarSonido();     
        cargarBotones();    
        cargarMusica();     
    }

    // Carga la imagen de fondo y la ajusta al tamaño de la pantalla
    private void cargarFondo() {
        Texture fondoTexture = new Texture(Gdx.files.internal("graphics/ui/menu/main/fondo_menu.png"));
        fondo = new Image(fondoTexture);
        fondo.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(fondo);
    }

    // Carga la imagen del título, escalándola proporcionalmente y centrada en la parte superior
    private void cargarTitulo() {
        Texture tituloTexture = new Texture(Gdx.files.internal("graphics/ui/menu/main/title.png"));
        titulo = new Image(tituloTexture);

        float tituloWidth = Gdx.graphics.getWidth() * 0.6f;
        float aspectRatio = (float) tituloTexture.getHeight() / (float) tituloTexture.getWidth();
        float tituloHeight = tituloWidth * aspectRatio;

        titulo.setSize(tituloWidth, tituloHeight);
        titulo.setPosition((Gdx.graphics.getWidth() - tituloWidth) / 2f,
                           Gdx.graphics.getHeight() - tituloHeight);

        stage.addActor(titulo);
    }

    // Carga el sonido que se reproducirá al presionar cualquier botón
    private void cargarSonido() {
        buttonSound = Gdx.audio.newSound(Gdx.files.internal("audio/Fx/Sbottom.mp3"));
    }

    // Crea los botones usando la clase Button con sonido integrado y los agrega al stage
    private void cargarBotones() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        float buttonWidth = screenW * 0.28f;  // 20% del ancho de la pantalla
        float buttonHeight = screenH * 0.17f; // 12% del alto de la pantalla

        float yTop = titulo.getY() - 20; // 20 píxeles debajo del título
        float yBottom = 20;
        int numButtons = 4;
        float spacing = (yTop - yBottom) / (numButtons + 1);
        float centerX = (screenW - buttonWidth) / 2f; // Centrado horizontal

        // Crear los botones con acción y sonido
        Button btnPlay = new Button("play", centerX, yTop - spacing, buttonWidth, buttonHeight,
                                    "graphics/ui/menu/main/", () -> System.out.println("PLAY"), buttonSound);

        Button btnLoadGame = new Button("loadgame", centerX, yTop - spacing * 2, buttonWidth, buttonHeight,
                                        "graphics/ui/menu/main/", () -> System.out.println("LOAD GAME"), buttonSound);

        Button btnOptions = new Button("options", centerX, yTop - spacing * 3, buttonWidth, buttonHeight,
                                       "graphics/ui/menu/main/", () -> System.out.println("OPTIONS"), buttonSound);

        Button btnExit = new Button("exit", centerX, yTop - spacing * 4, buttonWidth, buttonHeight,
                                    "graphics/ui/menu/main/", Gdx.app::exit, buttonSound);

        // Agregar botones al stage
        stage.addActor(btnPlay.getButton());
        stage.addActor(btnLoadGame.getButton());
        stage.addActor(btnOptions.getButton());
        stage.addActor(btnExit.getButton());
    }

    // Carga y reproduce la música de fondo
    private void cargarMusica() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/Music/S1.mp3"));
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
        if (buttonSound != null) buttonSound.dispose();
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
