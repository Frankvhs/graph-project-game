package io.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.game.managers.Resources;

public class PauseMenu {
    public static final String BASE_PATH = "graphics/ui/menu/pause";
    
    private Stage stage;
    private Image background;
    private Sound buttonSound;
    private boolean isVisible;
    
    private Runnable onContinue;
    private Runnable onExitToMenu;
    private Runnable onExit;
    
    public PauseMenu(SpriteBatch batch, Runnable onContinue, Runnable onExitToMenu, Runnable onExit) {
        this.onContinue = onContinue;
        this.onExitToMenu = onExitToMenu;
        this.onExit = onExit;
        this.isVisible = false;
        
        stage = new Stage(new ScreenViewport(), batch);
        
        loadAssets();
        loadSounds();
        createMenu();
    }
    
    private void loadAssets() {
        // Cargar texturas de botones
        Button.loadDrawable("continue", BASE_PATH);
        Button.loadDrawable("exit_to_menu", BASE_PATH);
        Button.loadDrawable("exit", BASE_PATH);
        
        // Cargar fondo del menú de pausa
        Resources.loadTexture("pause_menu", BASE_PATH);
        
        Resources.finish();
    }
    
    private void loadSounds() {
        buttonSound = Gdx.audio.newSound(Gdx.files.internal("audio/sound/Sbottom.mp3"));
    }
    
    private void createMenu() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        
        // Fondo más estrecho centrado
        background = new Image(getTexture("pause_menu"));
        float bgWidth = screenW * 0.38f;  // 38% del ancho de pantalla
        float bgHeight = screenH * 0.62f;  // 62% del alto de pantalla
        background.setSize(bgWidth, bgHeight);
        background.setPosition((screenW - bgWidth) / 2, (screenH - bgHeight) / 2);
        stage.addActor(background);
        
        // Tabla para organizar los botones
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);
        
        // Tamaño de los botones - más altos
        float buttonWidth = screenW * 0.3f;
        float buttonHeight = screenH * 0.16f;  // Más alto (era 0.12f)
        float spacing = 20f;
        
        // Botón continuar
        Button continueBtn = new Button("continue", BASE_PATH, () -> {
            hide();
            if (onContinue != null) onContinue.run();
        }, buttonSound);
        table.add(continueBtn).width(buttonWidth).height(buttonHeight).padBottom(spacing).row();
        
        // Botón salir al menú
        Button exitToMenuBtn = new Button("exit_to_menu", BASE_PATH, () -> {
            hide();
            if (onExitToMenu != null) onExitToMenu.run();
        }, buttonSound);
        table.add(exitToMenuBtn).width(buttonWidth).height(buttonHeight).padBottom(spacing).row();
        
        // Botón salir del juego
        Button exitBtn = new Button("exit", BASE_PATH, () -> {
            if (onExit != null) onExit.run();
        }, buttonSound);
        table.add(exitBtn).width(buttonWidth).height(buttonHeight).row();
    }
    
    private Texture getTexture(String name) {
        return Resources.getTexture(name, BASE_PATH);
    }
    
    public void show() {
        isVisible = true;
        Gdx.input.setInputProcessor(stage);
    }
    
    public void hide() {
        isVisible = false;
        Gdx.input.setInputProcessor(null);
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public Stage getStage() {
        return stage;
    }
    
    public void toggle() {
        if (isVisible) {
            hide();
        } else {
            show();
        }
    }
    
    public void render(float delta) {
        if (isVisible) {
            stage.act(delta);
            stage.draw();
        }
    }
    
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        // Actualizar tamaño y posición del fondo
        float bgWidth = width * 0.38f;
        float bgHeight = height * 0.62f;
        background.setSize(bgWidth, bgHeight);
        background.setPosition((width - bgWidth) / 2, (height - bgHeight) / 2);
    }
    
    public void dispose() {
        stage.dispose();
        if (buttonSound != null) {
            buttonSound.dispose();
        }
    }
}
