package com.p1nero.invincible.mixin;

import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

/**
 * 自己控制充能
 */
@Mixin(value = ServerPlayerPatch.class, remap = false)
public abstract class ServerPlayerPatchMixin extends PlayerPatch<ServerPlayer> {
    @Inject(method = "gatherDamageDealt", at = @At("HEAD"), cancellable = true)
    private void invincible$modifyCharge(EpicFightDamageSource source, float amount, CallbackInfo ci) {
        if (this.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
            if (!InvincibleCapabilityProvider.get(this.getOriginal()).isNotCharge()) {
                SkillContainer container = this.getSkill(SkillSlots.WEAPON_INNATE);
                if (!container.isFull()) {
                    float value = container.getResource() + amount;
                    if (value > 0.0F) {
                        container.getSkill().setConsumptionSynchronize((ServerPlayerPatch) (Object) this, value);
                    }
                }
            }
            ci.cancel();
        }
    }
}
