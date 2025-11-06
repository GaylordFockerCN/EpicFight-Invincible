package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.neoforgeevent.DuplicateAnimationRegistryEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.ComboAttackAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;

/**
 * 演示重新注册器
 */
@EventBusSubscriber(modid = InvincibleMod.MOD_ID)
public class InvincibleDuplicateDemoAnimations {

    public static AnimationManager.AnimationAccessor<ComboAttackAnimation> SWORD_AUTO1;

    @SubscribeEvent
    public static void registerAnimations(DuplicateAnimationRegistryEvent event) {
        event.newBuilder(InvincibleMod.MOD_ID, (builder) -> {
            SWORD_AUTO1 = builder.nextAccessor(Animations.SWORD_AUTO1, (accessor) ->
                    new ComboAttackAnimation(0.1F, 0.0F, 0.1F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
                            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
                            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ((self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.5F)));

        });
    }

}
