package com.p1nero.invincible.mixin;

import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.capability.InvinciblePlayer;
import com.p1nero.invincible.skill.ComboBasicAttack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.LinkAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * 动画的末尾重置状态，即各个事件
 */
@Mixin(value = StaticAnimation.class, remap = false)
public abstract class StaticAnimationMixin extends DynamicAnimation{
    @Inject(method = "end", at = @At("HEAD"))
    private void invincible$onAnimationEnd(LivingEntityPatch<?> entityPatch, DynamicAnimation nextAnimation, boolean isEnd, CallbackInfo ci){
        if(entityPatch instanceof PlayerPatch<?> playerPatch && playerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack && !this.isLinkAnimation()){
            InvinciblePlayer invinciblePlayer = InvincibleCapabilityProvider.get(playerPatch.getOriginal());
            invinciblePlayer.clear();
        }
    }
}
