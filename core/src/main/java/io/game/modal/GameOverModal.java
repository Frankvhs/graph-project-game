package io.game.modal;

import com.badlogic.gdx.Gdx;
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

    private Image background;
    private Image title;
    private Image message;

    public GameOverModal(Stage stage, Listener listener) {
        this.stage = stage;
        this.listener = listener;
        this.root = new Group();

        float sw = stage.getWidth();
        float sh = stage.getHeight();

        // Cargar texturas
        Texture bgTex = Resources.getTexture("fondo_game_over", "graphics/ui/menu/game_over");
        Texture titleTex = Resources.getTexture("game_over", "graphics/ui/menu/game_over");
        Texture msgTex = Resources.getTexture("message", "graphics/ui/menu/game_over");

        // Escala proporcional basada en 1920x1080 (en prueba)
        float scale = Math.min(sw / 1920f, sh / 1080f);

        // Fondo (pegado a los lados y abajo)
        background = new Image(bgTex);
        float bgWidth = sw; // ancho igual a pantalla
        float bgHeight = bgTex.getHeight() * (bgWidth / bgTex.getWidth()); // escalar proporcional
        background.setSize(bgWidth, bgHeight);
        background.setPosition(0, 0); // pegado a los lados y abajo

        // GAME OVER
        title = new Image(titleTex);
        title.setSize(titleTex.getWidth() * scale, titleTex.getHeight() * scale);
        title.setPosition(
                (sw - title.getWidth()) / 2f,
                sh / 2f + 50 * scale
        );

        // Mensaje debajo del título
        message = new Image(msgTex);
        message.setSize(msgTex.getWidth() * scale, msgTex.getHeight() * scale);
        message.setPosition(
                (sw - message.getWidth()) / 2f,
                title.getY() - message.getHeight() - 20 * scale
        );

        root.addActor(background);
        root.addActor(title);
        root.addActor(message);
    }

    public void show() {
        if (!root.hasParent()) stage.addActor(root);
    }

    public void hide() {
        root.remove();
    }

    public boolean isVisible() {
        return root.hasParent();
    }

    public void update() {
        // SPACE = reiniciar
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            listener.onRestart();
        }

        // ESC = salir a menú
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            listener.onExitToMenu();
        }
    }
}
