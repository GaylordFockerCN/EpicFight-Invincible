package com.p1nero.invincible.mixin;

import com.p1nero.invincible.skill.ComboBasicAttack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.SkillExecuteEvent;

@Mixin(value = SkillContainer.class, remap = false)
public abstract class SkillContainerMixin {
    @Shadow
    public abstract Skill getSkill();

    @Inject(method = "sendExecuteRequest", at = @At("HEAD"), cancellable = true)
    private void invincible$injectSendExecuteRequest(LocalPlayerPatch executer, ControllEngine controllEngine, CallbackInfoReturnable<SkillExecuteEvent> cir) {
        if (this.getSkill() instanceof ComboBasicAttack) {
            cir.setReturnValue(new SkillExecuteEvent(executer, (SkillContainer) (Object) this));
        }
    }
}
