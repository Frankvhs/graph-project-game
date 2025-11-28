package io.game.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.game.managers.Resources;

public class AnimatedEntity extends Entity {
	protected Animation<TextureRegion> animation;
	private float animationState = 0f;
	private float animationDuration = 0;

	public void play(Animation<TextureRegion> anim) {
		if (anim != this.animation) {
			this.animation = anim;
			animationState = 0f;
			animationDuration = anim.getAnimationDuration();
		}
	}
	public void play(String name, String basePath) {
		play(Resources.getAnimation(name, basePath));
	}

	/**
	 * Actualizar animación y mover la entidad segun su velocidad
	 */
	public void update(float delta) {

        if (animation != null) {
            animationState += delta * animation.getAnimationDuration() / Math.max(animationDuration, 0.0001f);
            this.frame = getFrame();
        }

        super.update(delta);
	}

	public TextureRegion getFrame() {
		return animation.getKeyFrame(animationState, true);
	}
	
	public void setAnimationDuration(float animationDuration) {
		this.animationDuration = animationDuration;
		
	}

	public boolean isAnimationFinished() {
		return animation.isAnimationFinished(animationState);
	}

}
