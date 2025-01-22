package com.p1nero.invincible.mixin;

import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.skill.ComboBasicAttack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = AttackAnimation.class, remap = false)
public class AttackAnimationMixin {
    @Inject(method = "getPlaySpeed", at = @At("RETURN"), cancellable = true)
    private void onGetPlaySpeed(LivingEntityPatch<?> entityPatch, DynamicAnimation animation, CallbackInfoReturnable<Float> cir) {
        if (entityPatch instanceof PlayerPatch<?> playerPatch && playerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
            playerPatch.getOriginal().getCapability(InvincibleCapabilityProvider.INVINCIBLE_PLAYER).ifPresent((invinciblePlayer -> {
                if (invinciblePlayer.getPlaySpeed() != 0) {
                    cir.setReturnValue(cir.getReturnValue() * invinciblePlayer.getPlaySpeed());
                }
            }));
        }
    }
}