package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.forgeevent.DuplicateAnimationRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.BasicAttackAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;

/**
 * 演示重新注册器
 */
@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InvincibleDuplicateDemoAnimations {

    public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SWORD_AUTO1;

    @SubscribeEvent
    public static void registerAnimations(DuplicateAnimationRegistryEvent event) {
        event.newBuilder(InvincibleMod.MOD_ID, (builder) -> {
            SWORD_AUTO1 = builder.nextAccessor(Animations.SWORD_AUTO1, (accessor) ->
                    new BasicAttackAnimation(0.1F, 0.0F, 0.1F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
                            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
                            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ((self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.5F)));

        });
    }

}
