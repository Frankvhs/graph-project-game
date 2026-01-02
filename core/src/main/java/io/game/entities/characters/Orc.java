package io.game.entities.characters;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import io.game.components.CombatComponent;
import io.game.components.HealthComponent;
import io.game.managers.Resources;

public class Orc extends Character {

	public static final String BASE_PATH = "graphics/sprites/characters/orc";

	private Vector2 targetPosition = new Vector2();
	private float detectionRange = 300f; // rango de detección del jugador
	private float attackRange = 50f; // rango para atacar
	private float wanderTimer = 0f;
	private float wanderInterval = 2f; // cambiar dirección cada 2 segundos
	private Vector2 wanderDirection = new Vector2();
	
	private enum State {
		IDLE,
		WANDER,
		CHASE,
		ATTACK
	}
	
	private State currentState = State.IDLE;

	/**
	 * Carga inicial de animaciones (llamar una vez en el setup del juego)
	 */
	public static void loadTextures() {
		Resources.loadAnimation("idle", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.LOOP);
		Resources.loadAnimation("walk", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.LOOP);
		Resources.loadAnimation("hurt", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.LOOP);
		Resources.loadAnimation("death", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.NORMAL);
		Resources.loadAnimation("attack01", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.NORMAL);

		Resources.finish();
	}

	public Orc(float x, float y) {
		super();
		size.set(100, 100);
		position.set(x, y);
		maxSpeed = 200; // más lento que el jugador
		
		// Componentes de combate y salud
		combat = new CombatComponent(20, 1.0f, attackRange);
		health = new HealthComponent(50);

		play("idle", BASE_PATH);
	}

	/**
	 * IA del orco: persigue al jugador si está cerca, deambula aleatoriamente,
	 * y ataca cuando está en rango
	 */
	public void updateAI(float dt, Vector2 playerPosition) {
		if (health.isDead()) {
			movement.set(0, 0);
			if (!this.animation.equals(Resources.getAnimation("death", BASE_PATH))) {
				play("death", BASE_PATH);
			}
			return;
		}
		
		float distanceToPlayer = position.dst(playerPosition);
		
		// Determinar estado según distancia
		if (distanceToPlayer <= attackRange) {
			currentState = State.ATTACK;
		} else if (distanceToPlayer <= detectionRange) {
			currentState = State.CHASE;
		} else {
			// Deambular o estar idle
			wanderTimer -= dt;
			if (wanderTimer <= 0) {
				wanderTimer = wanderInterval;
				// 50% de probabilidad de moverse o quedarse quieto
				if (Math.random() < 0.5f) {
					currentState = State.WANDER;
					// Dirección aleatoria
					wanderDirection.set(
						(float)(Math.random() * 2 - 1),
						(float)(Math.random() * 2 - 1)
					).nor();
				} else {
					currentState = State.IDLE;
				}
			}
		}
		
		// Ejecutar comportamiento según estado
		switch (currentState) {
			case IDLE:
				movement.set(0, 0);
				if (!combat.isAttacking()) {
					play("idle", BASE_PATH);
				}
				break;
				
			case WANDER:
				movement.set(wanderDirection).scl(maxSpeed * 0.5f);
				if (!combat.isAttacking()) {
					play("walk", BASE_PATH);
				}
				break;
				
			case CHASE:
				// Perseguir al jugador
				targetPosition.set(playerPosition);
				Vector2 direction = targetPosition.sub(position).nor();
				movement.set(direction).scl(maxSpeed);
				
				if (!combat.isAttacking()) {
					play("walk", BASE_PATH);
				}
				break;
				
			case ATTACK:
				// Detenerse y atacar
				movement.set(0, 0);
				
				// Intentar atacar
				if (combat.tryAttack()) {
					play("attack01", BASE_PATH);
					this.setAnimationDuration(combat.getCooldown());
				}
				
				// Orientarse hacia el jugador
				if (playerPosition.x < position.x) {
					flipLeft = true;
				} else {
					flipLeft = false;
				}
				break;
		}
	}

	@Override
	public void update(float dt) {
		super.update(dt);
	}
	
	/**
	 * Verifica si el orco puede atacar al jugador (está en rango y atacando)
	 */
	public boolean canDamagePlayer(Vector2 playerPosition) {
		if (!combat.isAttacking()) return false;
		float distance = position.dst(playerPosition);
		return distance <= combat.getAttackRange();
	}
	
	/**
	 * Recibir daño
	 */
	public void takeDamage(int damage) {
		if (health.isDead()) return;
		
		health.damage(damage);
		if (!health.isDead()) {
			play("hurt", BASE_PATH);
			this.setAnimationDuration(0.3f);
		}
	}
}
