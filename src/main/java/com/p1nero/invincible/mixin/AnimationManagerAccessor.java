package com.p1nero.invincible.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.Map;

@Mixin(AnimationManager.class)
public interface AnimationManagerAccessor {

    @Accessor(value = "animationByName", remap = false)
    Map<ResourceLocation, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getAnimationByName();

    @Accessor(value = "animationById", remap = false)
    Map<Integer, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getAnimationById();

    @Accessor(value = "animations", remap = false)
    Map<AnimationManager.AnimationAccessor<? extends StaticAnimation>, StaticAnimation> getAnimations();

}
