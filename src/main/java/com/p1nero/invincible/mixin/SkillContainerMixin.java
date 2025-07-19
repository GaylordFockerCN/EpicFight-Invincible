package com.p1nero.invincible.mixin;

import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(SkillContainer.class)
public abstract class SkillContainerMixin {
    @Shadow(remap = false) protected Skill containingSkill;

    @Inject(method = "requestExecute", at = @At("HEAD"), cancellable = true, remap = false)
    private void invincible$requestExecute(ServerPlayerPatch executor, FriendlyByteBuf buf, CallbackInfoReturnable<Boolean> cir){
        if(this.containingSkill instanceof ComboBasicAttack) {
            if(this.containingSkill.canExecute((SkillContainer) (Object) this) && this.containingSkill.isExecutableState(executor)) {
                this.containingSkill.executeOnServer((SkillContainer) (Object) this, buf);
                cir.setReturnValue(true);
            }
            cir.setReturnValue(false);
        }
    }
}
