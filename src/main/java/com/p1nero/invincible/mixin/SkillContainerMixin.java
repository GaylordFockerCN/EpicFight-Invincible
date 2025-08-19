package com.p1nero.invincible.mixin;

import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.nbt.CompoundTag;
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

    @Shadow(remap = false)
    protected Skill skill;

    @Inject(method = "requestCasting", at = @At("HEAD"), cancellable = true, remap = false)
    private void invincible$requestCasting(ServerPlayerPatch executor, CompoundTag args, CallbackInfoReturnable<Boolean> cir){
        if(this.skill instanceof ComboBasicAttack) {
            if(this.skill.canExecute((SkillContainer) (Object) this) && this.skill.isExecutableState(executor)) {
                this.skill.executeOnServer((SkillContainer) (Object) this, args);
                cir.setReturnValue(true);
            }
            cir.setReturnValue(false);
        }
    }
}
