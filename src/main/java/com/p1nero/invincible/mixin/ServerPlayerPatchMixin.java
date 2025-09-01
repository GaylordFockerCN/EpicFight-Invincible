package com.p1nero.invincible.mixin;

import com.p1nero.invincible.skill.ComboBasicAttack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.DealDamageEvent;

@Mixin(ServerPlayerPatch.class)
public class ServerPlayerPatchMixin {
    @Inject(method = "lambda$onJoinWorld$2", at = @At("HEAD"), cancellable = true, remap = false)
    private void invincible$onDealDamageEvent(DealDamageEvent.Damage dealDamageEvent, CallbackInfo ci) {
        if(dealDamageEvent.getPlayerPatch().getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
            ci.cancel();
        }
    }
}
