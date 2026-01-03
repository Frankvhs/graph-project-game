package io.game.modal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import io.game.managers.Resources;
import io.game.ui.Button;

public class PauseModal {

    public interface Listener {
        void onContinue();
        void onExitToMenu();
        void onExitGame();
    }

    private final Stage stage;
    private final Group root;
    private boolean isVisible = false;
    private Sound clickSound;
    
    private Image background;
    private Button btnContinue;
    private Button btnExitMenu;
    private Button btnExit;
    
    private String PATH = "graphics/ui/menu/pause";

    public PauseModal(Stage stage, Listener listener) {
        this.stage = stage;
        this.root = new Group();
        root.setTouchable(Touchable.enabled);
        
        // Cargar el sonido de click
        clickSound = Gdx.audio.newSound(Gdx.files.internal("audio/sound/Sbottom.mp3"));

        // Cargar texturas necesarias para los botones
        loadTextures();
        
        // Crear elementos del modal
        createModalElements(listener);
        
        // Posicionar elementos
        positionElements();
        
        // Asegurar que el grupo ocupe toda la pantalla
        root.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Ocultar inicialmente
        root.setVisible(false);
    }
    
    /**
     * Cargar todas las texturas necesarias
     */
    private void loadTextures() {
        Resources.loadTexture("continue_normal", PATH);
        Resources.loadTexture("continue_hover", PATH);
        Resources.loadTexture("continue_click", PATH);
        
        Resources.loadTexture("exit_to_menu_normal", PATH);
        Resources.loadTexture("exit_to_menu_hover", PATH);
        Resources.loadTexture("exit_to_menu_click", PATH);
        
        Resources.loadTexture("exit_normal", PATH);
        Resources.loadTexture("exit_hover", PATH);
        Resources.loadTexture("exit_click", PATH);
        
        Resources.loadTexture("pause_menu", PATH);
        Resources.finish();
    }
    
    /**
     * Calcular el factor de escalado basado en la resolución actual
     */
    private float calculateScale() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        float baseWidth = 1920f; // Resolución base para diseño
        float baseHeight = 1080f;
        
        // Escalar basado en el ancho y alto, usar el menor para que quepa completo
        float widthScale = screenWidth / baseWidth;
        float heightScale = screenHeight / baseHeight;
        
        // Usar el menor para asegurar que todo quepa en pantalla
        return Math.min(widthScale, heightScale);
    }
    
    /**
     * Crear los elementos del modal
     */
    private void createModalElements(Listener listener) {
        // Fondo del modal
        Texture bgTex = Resources.getTexture("pause_menu", PATH);
        if (bgTex == null) {
            Gdx.app.error("PauseModal", "No se pudo cargar la textura: pause_menu");
            return;
        }
        
        background = new Image(bgTex);
        
        // Botón Continuar
        btnContinue = new Button(
            "continue", 
            PATH,
            () -> {
                if (listener != null) listener.onContinue();
            },
            clickSound
        );
        
        // Botón Salir al Menú
        btnExitMenu = new Button(
            "exit_to_menu",
            PATH,
            () -> {
                if (listener != null) listener.onExitToMenu();
            },
            clickSound
        );
        
        // Botón Salir del Juego
        btnExit = new Button(
            "exit",
            PATH,
            () -> {
                if (listener != null) listener.onExitGame();
            },
            clickSound
        );
        
        root.addActor(background);
        root.addActor(btnContinue);
        root.addActor(btnExitMenu);
        root.addActor(btnExit);
    }
    
    /**
     * Redimensionar y posicionar todos los elementos
     */
    private void positionElements() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float scale = calculateScale();
        
        // Redimensionar fondo
        float bgScaleFactor = 0.6f; // Factor para hacer el fondo mas pequeño o mas grande
        Texture bgTex = ((TextureRegionDrawable) background.getDrawable()).getRegion().getTexture();
        float bgWidth = bgTex.getWidth() * scale * bgScaleFactor;
        float bgHeight = bgTex.getHeight() * scale * bgScaleFactor;
        background.setSize(bgWidth, bgHeight);
        
        // Posicionar fondo centrado
        background.setPosition(
            (screenWidth - bgWidth) / 2f,
            (screenHeight - bgHeight) / 2f
        );
        
        // Redimensionar botones
        float buttonScaleFactor = 1.5f; // Factor para hacer los botones mas pequeños o mas grandes
        float buttonWidth = 250 * scale * buttonScaleFactor;
        float buttonHeight = 80 * scale * buttonScaleFactor;
        
        btnContinue.setSize(buttonWidth, buttonHeight);
        btnExitMenu.setSize(buttonWidth, buttonHeight);
        btnExit.setSize(buttonWidth, buttonHeight);
        
        // Calcular posición de botones centrados verticalmente en el fondo
        float backgroundCenterX = background.getX() + background.getWidth() / 2f;
        float backgroundCenterY = background.getY() + background.getHeight() / 2f;
        
        // Altura total de los 3 botones con espaciado
        float buttonSpacing = 40 * scale; // Espacio entre botones
        float totalButtonsHeight = (3 * buttonHeight) + (2 * buttonSpacing);
        
        // Posición inicial del primer botón
        float firstButtonY = backgroundCenterY + (totalButtonsHeight / 2) - buttonHeight;
        
        // Posicionar botones centrados tanto horizontal como verticalmente en el fondo
        btnContinue.setPosition(
            backgroundCenterX - buttonWidth / 2f,
            firstButtonY
        );
        
        btnExitMenu.setPosition(
            backgroundCenterX - buttonWidth / 2f,
            firstButtonY - buttonHeight - buttonSpacing
        );
        
        btnExit.setPosition(
            backgroundCenterX - buttonWidth / 2f,
            firstButtonY - 2 * (buttonHeight + buttonSpacing)
        );
    }

    public void show() {
        if (!root.hasParent()) {
            stage.addActor(root);
        }
        
        // Recalcular posición y tamaño
        positionElements();
        
        root.setVisible(true);
        root.toFront();
        isVisible = true;
    }

    public void hide() {
        root.setVisible(false);
        root.remove();
        isVisible = false;
    }

    public void toggle() {
        if (isVisible) {
            hide();
        } else {
            show();
        }
    }

    public boolean isVisible() {
        return isVisible;
    }
    
    public void dispose() {
        if (clickSound != null) {
            clickSound.dispose();
            clickSound = null;
        }
        if (root.hasParent()) {
            root.remove();
        }
    }
}