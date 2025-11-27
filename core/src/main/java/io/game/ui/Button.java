package io.game.ui;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import io.game.managers.Resources;

public class Button extends ImageButton {
	public Button(String name, String basePath, Runnable accion,
			Sound clickSound) {
		super(getDrawable(name, basePath));

		addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float px, float py) {
				if (clickSound != null) {
					clickSound.play();
				}
				accion.run();
			}
		});
	}
	
	public Button(String name, float x, float y, float width, float height, String basePath, Runnable accion,
			Sound clickSound) {
		this(name, basePath, accion, clickSound);
		setSize(width, height);
		setPosition(x, y);

	}
	
	public float setAutoHeight () {
		float height = getWidth() * getMinHeight()/getMinWidth();
		setHeight(height);
		return height;
	}
	
    /**
     * Cargar en Resources el set de texturas de un boton
     */
    public static void loadDrawable(String name, String basePath) {
        Resources.loadTexture(name + "_normal", basePath);
        Resources.loadTexture(name + "_hover", basePath);
        Resources.loadTexture(name + "_click", basePath);
    }

    /**
     * Obtener el set de texturas de un boton
     */
	private static ImageButtonStyle getDrawable(String name, String basePath) {
		ImageButtonStyle style = new ImageButtonStyle();
		style.up = new TextureRegionDrawable(Resources.getTexture(name + "_normal", basePath));
		style.over = new TextureRegionDrawable(Resources.getTexture(name + "_hover", basePath));
		style.down = new TextureRegionDrawable(Resources.getTexture(name + "_click", basePath));

		return style;
	}
}
