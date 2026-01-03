package io.game.entities.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import io.game.components.CombatComponent;
import io.game.components.HealthComponent;
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
		position.set(0, 0);
		maxSpeed = 400;
		combat = new CombatComponent(100, 0.3f, 10);
		health = new HealthComponent(100);

		play("idle", BASE_PATH);
	}

	@Override
	public void update(float dt) {
		// Si está muerto, solo reproducir animación de muerte
		if (!isAlive()) {
			// Simplemente actualizar la animación existente
			super.update(dt);
			return;
		}

		// Movimiento normal
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
	
	public boolean wantsNextLevel() {	
		return Gdx.input.isKeyJustPressed(Input.Keys.E);
    }
    
    /**
     * Verifica si el jugador está vivo
     */
    public boolean isAlive() {
        return health != null && !health.isDead();
    }
    
    /**
     * Función para matar al jugador (para pruebas con tecla F)
     */
    public void kill() {
        if (health != null) {
            health.damage(health.getHealth()); // Daño igual a la salud actual
            play("death", BASE_PATH); // Cambiar a animación de muerte
        }
    }
    
    /**
     * Resetear jugador
     */
    public void reset() {
        // Resetear salud
        health = new HealthComponent(100);
        
        // Resetear combate
        combat = new CombatComponent(100, 0.3f, 10);
        
        // Resetear animación
        play("idle", BASE_PATH);
    }
}