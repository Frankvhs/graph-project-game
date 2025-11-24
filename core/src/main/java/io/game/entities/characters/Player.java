package io.game.entities.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.game.managers.Resources;

public class Player extends Character {

    public static final String BASE_PATH = "graphics/sprites/characters/player";
    private float moveSpeed = 200f;

    /**
     * Carga inicial de animaciones (llamar una vez en el setup del juego)
     */
    public static void loadTextures () {
        Resources.loadAnimation("idle", BASE_PATH, 100, 100, 0.1f, 35, 35);
        Resources.loadAnimation("walk", BASE_PATH, 100, 100, 0.1f, 35, 35);
        Resources.loadAnimation("hurt", BASE_PATH, 100, 100, 0.1f, 35, 35);
        Resources.loadAnimation("death", BASE_PATH, 100, 100, 0.1f, 35, 35);
        Resources.loadAnimation("attack02", BASE_PATH, 100, 100, 0.1f, 35, 35);
        Resources.loadAnimation("attack01", BASE_PATH, 100, 100, 0.1f, 35, 35);

        Resources.finish();
    }

    public Player() {
        super();
        size.set(100, 100);
        maxSpeed = 100;

        play("idle", BASE_PATH);
    }

    private void handleKeyPressed(float dt) {
        boolean up    = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down  = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        movement.set(0, 0);
        if (up)    movement.y += maxSpeed;
        if (down)  movement.y -= maxSpeed;
        if (left)  movement.x -= maxSpeed;
        if (right) movement.x += maxSpeed;
    }


    @Override
    public void update(float dt) {
        handleKeyPressed(dt);
        
        if (!movement.isZero()) {
            play("walk", BASE_PATH);
        } else {
            play("idle", BASE_PATH);
        }
        
        super.update(dt);
    }
}
