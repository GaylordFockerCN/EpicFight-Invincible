package com.p1nero.invincible.mixin;

import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.capability.InvinciblePlayer;
import com.p1nero.invincible.skill.ComboBasicAttack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = AnimationPlayer.class, remap = false)
public abstract class AnimationPlayerMixin {
    @Shadow public abstract boolean isEnd();

    @Shadow protected float prevElapsedTime;

    @Shadow protected float elapsedTime;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lyesman/epicfight/api/animation/types/DynamicAnimation;getPlaySpeed(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lyesman/epicfight/api/animation/types/DynamicAnimation;)F"))
    private float invincible$onGetPlaySpeed(DynamicAnimation instance, LivingEntityPatch<?> entityPatch, DynamicAnimation animation) {
        if (entityPatch instanceof PlayerPatch<?> playerPatch && playerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
            InvinciblePlayer invinciblePlayer = InvincibleCapabilityProvider.get(playerPatch.getOriginal());
            if (invinciblePlayer.getPlaySpeedMultiplier() != 0) {
                return instance.getPlaySpeed(entityPatch, animation) * invinciblePlayer.getPlaySpeedMultiplier();
            }
        }
        return instance.getPlaySpeed(entityPatch, animation);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void invincible$injectTick(LivingEntityPatch<?> entityPatch, CallbackInfo ci){
        if (entityPatch instanceof ServerPlayerPatch serverPlayerPatch && serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
            serverPlayerPatch.getOriginal().getCapability(InvincibleCapabilityProvider.INVINCIBLE_PLAYER).ifPresent(invinciblePlayer -> {
                if (this.isEnd()) {
                    invinciblePlayer.clear();
                    return;
                }
                List<Integer> toRemove = new ArrayList<>();
                List<TimeStampedEvent> eventList = invinciblePlayer.getTimeEventList();
                for (int i = 0; i < eventList.size(); i++) {
                    TimeStampedEvent event = eventList.get(i);
                    if (!entityPatch.getOriginal().isAlive()) {
                        break;
                    }
                    event.testAndExecute(entityPatch, this.prevElapsedTime, this.elapsedTime);
                    if (event.isExecuted()) {
                        toRemove.add(i);
                    }
                }
                for (int i = toRemove.size() - 1; i >= 0; i--) {
                    eventList.remove((int) toRemove.get(i));
                }
                toRemove.clear();
            });
        }
    }

}