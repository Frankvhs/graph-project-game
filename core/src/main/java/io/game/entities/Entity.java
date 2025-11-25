package io.game.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Entity {
	final public Vector2 position = new Vector2();
	final public Vector2 movement = new Vector2();
	final public Vector2 size = new Vector2();
	final public Vector2 anchor = new Vector2();

	protected float rotation = 0f;
	protected TextureRegion frame = new TextureRegion();
	protected boolean flipLeft = false;

	public void setFrame(TextureRegion frame) {
		this.frame = frame;
	}

	/**
	 * Mover la entidad segun su valor de movimiento
	 */
	public void update(float delta) {
		// movement
		position.mulAdd(movement, delta);
	}

	public void render(Batch batch) {
		if (flipLeft) {
			batch.draw(
				frame, 
				position.x + size.x, position.y, 
				anchor.x, anchor.y, 
				- size.x, size.y, 
				1f, 1f, 
				rotation
			);
		} else {
			batch.draw(
				frame, 
				position.x, position.y, 
				anchor.x, anchor.y, 
				size.x, size.y, 
				1f, 1f, 
				rotation
			);
		}
	}
}
