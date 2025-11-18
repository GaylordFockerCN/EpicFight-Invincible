package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.animation.types.MultiPhaseAttackAnimation;
import com.p1nero.invincible.api.forgeevent.DuplicateAnimationRegistryEvent;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.BasicAttackAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;

/**
 * 演示重新注册器
 */
@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InvincibleDuplicateDemoAnimations {

    public static AnimationManager.AnimationAccessor<MultiPhaseAttackAnimation> SWORD_DUAL_AUTO3;
    public static AnimationManager.AnimationAccessor<MultiPhaseAttackAnimation> DANCING_EDGE;

    @SubscribeEvent
    public static void registerAnimations(DuplicateAnimationRegistryEvent event) {
        event.newBuilder(InvincibleMod.MOD_ID, (builder) -> {
            SWORD_DUAL_AUTO3 = builder.nextAccessor(Animations.SWORD_DUAL_AUTO3, (accessor) ->
                    new MultiPhaseAttackAnimation(0.1F, accessor, Armatures.BIPED,
                            new AttackAnimation.Phase(0.0F, 0.25F, 0.25F, 0.35F, 0.6F, Float.MAX_VALUE, InteractionHand.MAIN_HAND, Armatures.BIPED.get().toolR, null),
                            new AttackAnimation.Phase(0.0F, 0.25F, 0.25F, 0.35F, 0.6F, Float.MAX_VALUE, InteractionHand.OFF_HAND, Armatures.BIPED.get().toolL, null))
                            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
                            //演示用，故意减速
                            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ((self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.5F)));
            DANCING_EDGE = builder.nextAccessor(Animations.DANCING_EDGE, (accessor) ->
                     new MultiPhaseAttackAnimation(0.1F, accessor, Armatures.BIPED,
                             new AttackAnimation.Phase(0.0F, 0.25F, 0.4F, 0.4F, 0.4F, Armatures.BIPED.get().toolR, null),
                             new AttackAnimation.Phase(0.4F, 0.4F, 0.5F, 0.55F, 0.6F, InteractionHand.OFF_HAND, Armatures.BIPED.get().toolL, null),
                             new AttackAnimation.Phase(0.6F, 0.6F, 0.7F, 1.15F, Float.MAX_VALUE, Armatures.BIPED.get().toolR, null),
                             new AttackAnimation.Phase(0.0F, 0.25F, 0.4F, 0.4F, 0.4F, Armatures.BIPED.get().toolL, null),
                             new AttackAnimation.Phase(0.4F, 0.4F, 0.5F, 0.55F, 0.6F, InteractionHand.OFF_HAND, Armatures.BIPED.get().toolR, null),
                             new AttackAnimation.Phase(0.6F, 0.6F, 0.7F, 1.15F, Float.MAX_VALUE, Armatures.BIPED.get().toolL, null))
                            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F).addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
                            //演示用，故意减速
                            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, ((self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 0.5F)));

        });
    }

}
