package io.game.entities.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import io.game.components.CombatComponent;
import io.game.components.HealthComponent;
import io.game.managers.Resources;

public class Player extends Character {

	public static final String BASE_PATH = "graphics/sprites/characters/player";
	private boolean damageApplied = false; // Control para aplicar daño solo una vez por ataque
	private int keys = 0; // Cantidad de llaves que tiene el jugador
	private int flasks = 0; // Cantidad de frascos de curación

	/**
	 * Carga inicial de animaciones (llamar una vez en el setup del juego)
	 */
	public static void loadTextures() {
		Resources.loadAnimation("idle", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.LOOP);
		Resources.loadAnimation("walk", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.LOOP);
		Resources.loadAnimation("hurt", BASE_PATH, 100, 100, 0.1f, 35, 35, PlayMode.NORMAL);
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
		combat = new CombatComponent(10, 0.5f, 70); // 10 daño, 0.5s cooldown, 70 rango
		health = new HealthComponent(20);

		play("idle", BASE_PATH);
	}

	@Override
	public void update(float dt) {
		// Si está muerto, no procesar input ni movimiento
		if (health.isDead()) {
			movement.set(0, 0);
			// Reproducir animación de muerte si aún no se ha reproducido
			if (!this.animation.equals(Resources.getAnimation("death", BASE_PATH))) {
				play("death", BASE_PATH);
			}
			super.update(dt);
			return;
		}
		
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
		
		// Usar flask con tecla Q
		if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
			useFlask();
		}

		if (attack1 || attack2) {
			combat.tryAttack();
			if (combat.isAttacking()) {
			    play(attack1 ? "attack01" : "attack02", BASE_PATH);
			    this.setAnimationDuration(combat.getCooldown());
			    damageApplied = false; // Resetear para el nuevo ataque
			}
		}

		// Solo cambiar animación si no está atacando ni herido
		if (!combat.isAttacking() && !this.animation.equals(Resources.getAnimation("hurt", BASE_PATH))) {
			if (!movement.isZero()) {
				play("walk", BASE_PATH);
			} else {
				play("idle", BASE_PATH);
			}
		}
		
		// Si la animación de hurt terminó, volver a idle/walk
		if (this.animation.equals(Resources.getAnimation("hurt", BASE_PATH)) && isAnimationFinished()) {
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
     * Verifica si el jugador puede hacer daño a un enemigo
     */
    public boolean canDamageEnemy(com.badlogic.gdx.math.Vector2 enemyPosition) {
        if (damageApplied) return false;
        if (!combat.isAttacking()) return false;
        
        boolean isAttack1 = this.animation.equals(Resources.getAnimation("attack01", BASE_PATH));
        boolean isAttack2 = this.animation.equals(Resources.getAnimation("attack02", BASE_PATH));
        if (!isAttack1 && !isAttack2) return false;
        
        float progress = animationState / animation.getAnimationDuration();
        if (progress < 0.6f || progress > 0.8f) return false;
        
        float distance = position.dst(enemyPosition);
        if (distance <= combat.getAttackRange()) {
            damageApplied = true;
            return true;
        }
        return false;
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
    	} else {
    		play("death", BASE_PATH);
    	}
    }
    
    /**
     * Añadir una llave al inventario
     */
    public void addKey() {
    	keys++;
    }
    
    /**
     * Usar una llave (al abrir un cofre)
     */
    public boolean useKey() {
    	if (keys > 0) {
    		keys--;
    		return true;
    	}
    	return false;
    }
    
    /**
     * Obtener cantidad de llaves
     */
    public int getKeys() {
    	return keys;
    }
    
    /**
     * Resetear llaves (al reiniciar nivel)
     */
    public void resetKeys() {
    	keys = 0;
    }
    
    /**
     * Añadir un frasco de curación al inventario
     */
    public void addFlask() {
    	flasks++;
    }
    
    /**
     * Usar un frasco de curación (cura 5 puntos)
     */
    public boolean useFlask() {
    	if (flasks > 0 && !health.isDead() && health.getHealth() < health.getMaxHealth()) {
    		flasks--;
    		health.heal(5);
    		System.out.println("¡Flask usado! Vida: " + health.getHealth() + "/" + health.getMaxHealth() + " - Flasks restantes: " + flasks);
    		return true;
    	}
    	return false;
    }
    
    /**
     * Obtener cantidad de frascos
     */
    public int getFlasks() {
    	return flasks;
    }

}