package io.game.entities.characters;

import io.game.components.CombatComponent;
import io.game.components.HealthComponent;
import io.game.entities.AnimatedEntity;

public class Character extends AnimatedEntity {
    public HealthComponent health;
    public CombatComponent combat;
    
    protected float maxSpeed;
    protected boolean facingLeft = false;
    
    /**
     * - Girar a la izquierda o derecha segun movimiento
     * - Normalizar vector para que no supere la velocidad maxima
     * - Actualizar animación y mover la entidad segun su movimiento
     */
    public void update(float dt) {
    	if (movement.x > 0) {
    	    facingLeft = false;
    	} else if (movement.x < 0) {
    	    facingLeft = true;
    	}
    	
    	if (frame.isFlipX() != facingLeft) {
    	    frame.flip(true, false);
    	}
    	
    	if (!movement.isZero()) {
          	//esto es para normalizar el vector ;)
            movement.nor().scl(maxSpeed);
    	}
    	
    	super.update(dt);
    }
}
