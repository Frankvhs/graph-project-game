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
    
    public Entity() {}
    public Entity(float x, float y, float width, float height) {
    	position.set(x, y);
        size.set(width, height);
    }
    public Entity(TextureRegion frame, float x, float y, float width, float height) {
        this(x, y, width, height);
    	this.frame = frame;
    }
    
    public void setFrame (TextureRegion frame) {
    	this.frame = frame;
    }

    /**
     * Mover la entidad segun su velocidad
     * @param delta
     */
    public void update(float delta) {
        // movement
        position.mulAdd(movement, delta);
    }

    public void render(Batch batch) {
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
