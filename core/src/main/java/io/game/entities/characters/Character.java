package io.game.entities.characters;

import io.game.components.CombatComponent;
import io.game.components.HealthComponent;
import io.game.entities.AnimatedEntity;

public class Character extends AnimatedEntity {
    public HealthComponent health;
    public CombatComponent combat;
    
    protected float maxSpeed;
    
    public Character() {
    	super();
    	anchor.set(0.5f, 0.9f);
    }
    
    /**
     * - Girar a la izquierda o derecha segun movimiento
     * - Normalizar vector para que no supere la velocidad maxima
     * - Actualizar animación y mover la entidad segun su movimiento
     */
    public void update(float dt) {
    	if (movement.x > 0) {
    	    flipLeft = false;
    	} else if (movement.x < 0) {
    	    flipLeft = true;
    	}
    	
    	if (!movement.isZero()) {
          	//esto es para normalizar el vector ;)
            movement.nor().scl(maxSpeed);
    	}
    	
    	combat.update(dt);
    	super.update(dt);
    }
}
