package io.game.entities.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import io.game.components.CombatComponent;
import io.game.managers.Resources;

public class Player extends Character {

	public static final String BASE_PATH = "graphics/sprites/characters/player";

	/**
	 * Carga inicial de animaciones (llamar una vez en el setup del juego)
	 */
	public static void loadTextures() {
		Resources.loadAnimation("idle", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.LOOP);
		Resources.loadAnimation("walk", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.LOOP);
		Resources.loadAnimation("hurt", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.LOOP);
		Resources.loadAnimation("death", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.NORMAL);
		Resources.loadAnimation("attack01", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.NORMAL);
		Resources.loadAnimation("attack02", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.NORMAL);

		Resources.finish();
	}

	public Player() {
		super();
		size.set(100, 100);
		maxSpeed = 400;
		combat = new CombatComponent(100, 0.3f, 10);

		play("idle", BASE_PATH);
	}

	@Override
	public void update(float dt) {
		boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
		boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
		boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
		boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

		movement.set(0, 0);
		if (up)
			movement.y += maxSpeed;
		if (down)
			movement.y -= maxSpeed;
		if (left)
			movement.x -= maxSpeed;
		if (right)
			movement.x += maxSpeed;
		
		if ((movement.x + position.x) < 0 || (movement.x + position.x) > Gdx.graphics.getWidth()) movement.x = 0;
		if ((movement.y + position.y) < 0 || (movement.y + position.y) > Gdx.graphics.getHeight()) movement.y = 0;

		boolean attack1 = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
		boolean attack2 = Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT);

		if (attack1 || attack2) {
			combat.tryAttack();
			if (combat.isAttacking()) {
			    play(attack1 ? "attack01" : "attack02", BASE_PATH);
			    this.setAnimationDuration(combat.getCooldown());
			}
		}

		// Solo cambiar animación si no está atacando
		if (!combat.isAttacking()) {
			if (!movement.isZero()) {
				play("walk", BASE_PATH);
			} else {
				play("idle", BASE_PATH);
			}
		}

		super.update(dt);
	}

}