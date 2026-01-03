package io.game.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.game.managers.Resources;

public class AnimatedEntity extends Entity {
	protected Animation<TextureRegion> animation;
	protected float animationState = 0f;
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
	 * Fuerza el cambio de animación incluso si es la misma
	 */
	public void forcePlay(Animation<TextureRegion> anim) {
		if (anim == null) return;
		this.animation = anim;
		this.animationState = 0f;
		this.animationDuration = anim.getAnimationDuration();
	}
	public void forcePlay(String name, String basePath) {
		Animation<TextureRegion> anim = Resources.getAnimation(name, basePath);
		forcePlay(anim);
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
		if (animation == null) return null;
		// Usar looping basado en el PlayMode de la animación
		boolean looping = animation.getPlayMode() == com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP || 
		                  animation.getPlayMode() == com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP_PINGPONG ||
		                  animation.getPlayMode() == com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP_RANDOM ||
		                  animation.getPlayMode() == com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP_REVERSED;
		return animation.getKeyFrame(animationState, looping);
	}
	
	public void setAnimationDuration(float animationDuration) {
		this.animationDuration = animationDuration;
		
	}

	public boolean isAnimationFinished() {
		return animation.isAnimationFinished(animationState);
	}
	
	/**
	 * Verifica si la animación ha progresado al menos un cierto porcentaje
	 * @param progress Valor entre 0.0 y 1.0 (ej: 0.7 = 70%)
	 * @return true si la animación ha progresado al menos ese porcentaje
	 */
	public boolean isAnimationAtLeast(float progress) {
		if (animation == null) return false;
		float totalDuration = animation.getAnimationDuration();
		return animationState >= (totalDuration * progress);
	}

}
