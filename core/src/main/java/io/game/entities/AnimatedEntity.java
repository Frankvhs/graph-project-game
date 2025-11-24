package io.game.entities;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.game.managers.Resources;

public class AnimatedEntity extends Entity {
	private Animation<TextureRegion> animation;
	private float animationState = 0f;

	public Animation<TextureRegion> play(Animation<TextureRegion> anim) {
		if (anim != this.animation) {
			this.animation = anim;
			animationState = 0f;
		}
		return anim;
	}
	public Animation<TextureRegion> play(String name, String basePath) {
		return play(Resources.getAnimation(name, basePath));
	}

	/**
	 * Actualizar animación y mover la entidad segun su velocidad
	 */
	public void update(float delta) {
		animationState += delta;
		this.frame = getFrame();

		super.update(delta);
	}

	public TextureRegion getFrame() {
		return animation.getKeyFrame(animationState, true);
	}

	public boolean isAnimationFinished() {
		return animation.isAnimationFinished(animationState);
	}

}
