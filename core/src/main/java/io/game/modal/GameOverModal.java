package io.game.modal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import io.game.managers.Resources;

public class GameOverModal {

    public interface Listener {
        void onRestart();
        void onExitToMenu();
    }

    private final Stage stage;
    private final Group root;
    private final Listener listener;
    private boolean isVisible = false;

    private Image overlay;
    private Image background;
    private Image title;
    private Image message;

    private String PATH = "graphics/ui/menu/game_over";

    public GameOverModal(Stage stage, Listener listener) {
        this.stage = stage;
        this.listener = listener;
        this.root = new Group();

        // Cargar texturas
        loadTextures();
        
        // Crear elementos
        createElements();
        
        // Posicionar
        positionElements();
        
        // Asegurar que el grupo ocupe toda la pantalla
        root.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Ocultar inicialmente
        root.setVisible(false);
    }
    
    private void loadTextures() {
        Resources.loadTexture("fondo_game_over", PATH);
        Resources.loadTexture("game_over", PATH);
        Resources.loadTexture("message", PATH);
        Resources.finish();
    }

    private void createElements() {
        // Overlay oscuro
        Pixmap overlayPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        overlayPixmap.setColor(new Color(0, 0, 0, 0.5f));
        overlayPixmap.fill();
        overlay = new Image(new Texture(overlayPixmap));
        overlayPixmap.dispose();

        // Fondo (debe existir en graphics/ui/menu/game_over/fondo_game_over.png)
        Texture bgTex = Resources.getTexture("fondo_game_over", PATH);
        if (bgTex == null) {
            Gdx.app.error("GameOverModal", "ERROR: No se encontró fondo_game_over.png");
            bgTex = new Texture(1, 1, Pixmap.Format.RGBA8888);
        }
        background = new Image(bgTex);

        // Título GAME OVER
        Texture titleTex = Resources.getTexture("game_over", PATH);
        if (titleTex == null) {
            Gdx.app.error("GameOverModal", "ERROR: No se encontró game_over.png");
            titleTex = new Texture(1, 1, Pixmap.Format.RGBA8888);
        }
        title = new Image(titleTex);

        // Mensaje
        Texture msgTex = Resources.getTexture("message", PATH);
        if (msgTex == null) {
            Gdx.app.error("GameOverModal", "ERROR: No se encontró message.png");
            msgTex = new Texture(1, 1, Pixmap.Format.RGBA8888);
        }
        message = new Image(msgTex);

        // Agregar en orden correcto
        root.addActor(overlay);
        root.addActor(background);
        root.addActor(title);
        root.addActor(message);
    }

    private void positionElements() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // 1. Overlay - toda la pantalla
        overlay.setSize(screenW, screenH);
        overlay.setPosition(0, 0);

        // 2. Fondo
        // Calculamos altura proporcional manteniendo la relación de aspecto
        float bgWidth = screenW;
        float bgHeight = background.getHeight() * (bgWidth / background.getWidth());
        background.setSize(bgWidth, bgHeight);
        background.setPosition(0, 0); // Pegado abajo

        // 3. Título GAME OVER - CENTRADO exactamente en medio de la pantalla
        float titleWidth = screenW * 0.6f; // 60% del ancho de pantalla
        float titleHeight = title.getHeight() * (titleWidth / title.getWidth());
        title.setSize(titleWidth, titleHeight);
        title.setPosition(
            (screenW - titleWidth) / 2f,
            (screenH - titleHeight) / 2f
        );

        // 4. Mensaje - CENTRADO debajo del título
        float msgWidth = screenW * 0.4f; // 40% del ancho de pantalla
        float msgHeight = message.getHeight() * (msgWidth / message.getWidth());
        message.setSize(msgWidth, msgHeight);
        message.setPosition(
            (screenW - msgWidth) / 2f,
            title.getY() - msgHeight - 20 // 20px de separación
        );
    }

    public void show() {
        if (!root.hasParent()) {
            stage.addActor(root);
        }
        
        // Redimensionar por si cambió la pantalla
        resize();
        
        root.setVisible(true);
        isVisible = true;
    }

    public void hide() {
        root.setVisible(false);
        isVisible = false;
    }

    public void update() {
        // SPACE = reiniciar
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            Gdx.app.log("GameOverModal", "SPACE presionado - reiniciando");
            if (listener != null) listener.onRestart();
        }

        // ESC = salir a menú
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            Gdx.app.log("GameOverModal", "ESC presionado - saliendo al menú");
            if (listener != null) listener.onExitToMenu();
        }
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void resize() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        
        root.setSize(screenW, screenH);
        overlay.setSize(screenW, screenH);
        
        // Recalcular posiciones
        positionElements();
    }
    
    public void dispose() {
        if (root.hasParent()) {
            root.remove();
        }
    }
}