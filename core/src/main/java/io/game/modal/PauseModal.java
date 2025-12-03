package io.game.modal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

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

    public PauseModal(Stage stage, Listener listener) {
        this.stage = stage;
        this.root = new Group();

        float sw = stage.getWidth();
        float sh = stage.getHeight();
        String PATH = "graphics/ui/menu/pause";

        Texture bgTex = Resources.getTexture("pause_menu", PATH);

        // Escalado relativo a 1920x1080 (en prueba)
        float scale = Math.min(sw / 1920f, sh / 1080f);
        float bgWidth = bgTex.getWidth() * scale;
        float bgHeight = bgTex.getHeight() * scale;

        Image background = new Image(bgTex);
        background.setSize(bgWidth, bgHeight);
        background.setPosition(
            (sw - bgWidth) / 2f,
            (sh - bgHeight) / 2f
        );

        float centerX = background.getX() + background.getWidth() / 2f;

        Sound clickSound = Gdx.audio.newSound(Gdx.files.internal("audio/sound/Sbottom.mp3"));

        // Separación vertical proporcional (en prueba)
        float spacing = 90 * scale;

        // Continue
        Button btnContinue = new Button(
            "continue",
            PATH,
            listener::onContinue,
            clickSound
        );
        btnContinue.setSize(250 * scale, 80 * scale);
        btnContinue.setPosition(centerX - btnContinue.getWidth() / 2f,
                                background.getY() + background.getHeight() - spacing);

        // Exit to Menu
        Button btnExitMenu = new Button(
            "exit_to_menu",
            PATH,
            listener::onExitToMenu,
            clickSound
        );
        btnExitMenu.setSize(250 * scale, 80 * scale);
        btnExitMenu.setPosition(centerX - btnExitMenu.getWidth() / 2f,
                                btnContinue.getY() - spacing);

        // Exit Game
        Button btnExit = new Button(
            "exit",
            PATH,
            listener::onExitGame,
            clickSound
        );
        btnExit.setSize(250 * scale, 80 * scale);
        btnExit.setPosition(centerX - btnExit.getWidth() / 2f,
                            btnExitMenu.getY() - spacing);

        root.addActor(background);
        root.addActor(btnContinue);
        root.addActor(btnExitMenu);
        root.addActor(btnExit);
    }

    public void show() {
        if (!root.hasParent())
            stage.addActor(root);
    }

    public void hide() {
        root.remove();
    }

    public boolean isVisible() {
        return root.hasParent();
    }
}
