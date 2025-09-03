package com.p1nero.invincible.mixin;

import com.p1nero.invincible.skill.ComboBasicAttack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.neoevent.playerpatch.DealDamageEvent;
import yesman.epicfight.events.EntityEvents;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(EntityEvents.class)
public class EntityEventsMixin {

    @Inject(method = "lambda$epicfight$dealDamagePost$24", at = @At("HEAD"), cancellable = true)
    private static void invincible$epicfight$dealDamagePost(DealDamageEvent.Post event, ServerPlayerPatch playerPatch, CallbackInfo ci) {
        SkillContainer container = playerPatch.getSkill(SkillSlots.WEAPON_INNATE);
        if(container.getSkill() instanceof ComboBasicAttack) {
            ci.cancel();
        }
    }
}
