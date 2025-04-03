package com.p1nero.invincible.api.animation;

import yesman.epicfight.api.animation.types.StaticAnimation;

/**
 * This interface is for array use
 */
@FunctionalInterface
public interface StaticAnimationProvider extends AnimationProvider<StaticAnimation> {
    StaticAnimation get();
}