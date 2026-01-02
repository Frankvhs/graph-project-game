package io.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.game.managers.Resources;

public class GameOverScreen {
    public static final String BASE_PATH = "graphics/ui/menu/gameOver";
    
    private Stage stage;
    private Image background;
    private Image gameOverTitle;
    private Image message;
    private boolean isVisible;
    
    private Runnable onRestart;
    private Runnable onExit;
    
    public GameOverScreen(SpriteBatch batch, Runnable onRestart, Runnable onExit) {
        this.onRestart = onRestart;
        this.onExit = onExit;
        this.isVisible = false;
        
        stage = new Stage(new ScreenViewport(), batch);
        
        loadAssets();
        createUI();
    }
    
    private void loadAssets() {
        Resources.loadTexture("fondo_game_over", BASE_PATH);
        Resources.loadTexture("game_over", BASE_PATH);
        Resources.loadTexture("message", BASE_PATH);
        
        Resources.finish();
    }
    
    private void createUI() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        
        // Fondo oscuro
        background = new Image(getTexture("fondo_game_over"));
        background.setSize(screenW, screenH);
        stage.addActor(background);
        
        // Título "GAME OVER"
        gameOverTitle = new Image(getTexture("game_over"));
        float titleWidth = screenW * 0.6f;
        float titleHeight = titleWidth * gameOverTitle.getHeight() / gameOverTitle.getWidth();
        gameOverTitle.setSize(titleWidth, titleHeight);
        gameOverTitle.setPosition((screenW - titleWidth) / 2, screenH * 0.6f);
        stage.addActor(gameOverTitle);
        
        // Mensaje
        message = new Image(getTexture("message"));
        float msgWidth = screenW * 0.5f;
        float msgHeight = msgWidth * message.getHeight() / message.getWidth();
        message.setSize(msgWidth, msgHeight);
        message.setPosition((screenW - msgWidth) / 2, screenH * 0.3f);
        stage.addActor(message);
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
    
    public void render(float delta) {
        if (isVisible) {
            // Detectar tecla Space para reiniciar
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
                hide();
                if (onRestart != null) onRestart.run();
            }
            
            // Detectar tecla Esc para salir
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                if (onExit != null) onExit.run();
            }
            
            stage.act(delta);
            stage.draw();
        }
    }
    
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        
        float screenW = width;
        float screenH = height;
        
        background.setSize(screenW, screenH);
        
        float titleWidth = screenW * 0.6f;
        float titleHeight = titleWidth * getTexture("game_over").getHeight() / getTexture("game_over").getWidth();
        gameOverTitle.setSize(titleWidth, titleHeight);
        gameOverTitle.setPosition((screenW - titleWidth) / 2, screenH * 0.6f);
        
        float msgWidth = screenW * 0.5f;
        float msgHeight = msgWidth * getTexture("message").getHeight() / getTexture("message").getWidth();
        message.setSize(msgWidth, msgHeight);
        message.setPosition((screenW - msgWidth) / 2, screenH * 0.3f);
    }
    
    public void dispose() {
        stage.dispose();
    }
    
    public Stage getStage() {
        return stage;
    }
}
