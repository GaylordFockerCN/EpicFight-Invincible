package com.p1nero.invincible.mixin;

import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.types.entity.DealDamageEvent;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(ServerPlayerPatch.class)
public abstract class ServerPlayerPatchMixin extends PlayerPatch<ServerPlayer> {

    public ServerPlayerPatchMixin(ServerPlayer entity) {
        super(entity);
    }

    @Inject(method = "lambda$new$0", at = @At("HEAD"), cancellable = true)
    private void invincible$epicfight$dealDamagePost(DealDamageEvent.Post event, CallbackInfo ci) {
        SkillContainer container = this.getSkill(SkillSlots.WEAPON_INNATE);
        if(container.getSkill() instanceof ComboBasicAttack) {
            ci.cancel();
        }
    }
}
