package io.game.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class Button {

    private ImageButton button;  
    private Texture normal;      
    private Texture hover;       
    private Texture click;       
    private Sound clickSound;    

    /**
     *
     * @param name 
     * @param x 
     * @param y
     * @param width 
     * @param height
     * @param basePath 
     * @param accion 
     * @param clickSound 
     */
    public Button(String name, float x, float y, float width, float height, String basePath, Runnable accion, Sound clickSound) {
        // Cargar texturas del botón
        normal = new Texture(Gdx.files.internal(basePath + name + "_normal.png"));
        hover  = new Texture(Gdx.files.internal(basePath + name + "_hover.png"));
        click  = new Texture(Gdx.files.internal(basePath + name + "_click.png"));

        // Guardar el sonido del botón
        this.clickSound = clickSound;

        // Crear estilo del ImageButton con las tres texturas
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(normal);
        style.over = new TextureRegionDrawable(hover);
        style.down = new TextureRegionDrawable(click);

        // Crear el ImageButton con el estilo
        button = new ImageButton(style);
        button.setSize(width, height);
        button.setPosition(x, y);

        // Agregar listener de click
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float px, float py) {
                if (Button.this.clickSound != null) {
                    Button.this.clickSound.play();
                }
                accion.run();
            }
        });
    }

    public ImageButton getButton() {
        return button;
    }

    public void dispose() {
        normal.dispose();
        hover.dispose();
        click.dispose();
    }
}
