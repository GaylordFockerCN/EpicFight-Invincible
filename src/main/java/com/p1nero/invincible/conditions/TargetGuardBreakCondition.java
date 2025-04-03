package com.p1nero.invincible.conditions;

import net.minecraftforge.fml.ModList;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import static com.nameless.indestructible.main.Indestructible.NEUTRALIZE_ANIMATION_LIST;

public class TargetGuardBreakCondition implements Condition<ServerPlayerPatch> {

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        if(!ModList.get().isLoaded("indestructible")){
            throw new IllegalStateException("try to use TargetGuardBreakCondition without indestructible!");
        }
        LivingEntityPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(serverPlayerPatch.getTarget(), LivingEntityPatch.class);
        if(targetPatch == null) {
            return false;
        }
        return targetPatch.getEntityState().hurtLevel() > 1 && targetPatch.getAnimator().getPlayerFor(null).getAnimation() instanceof LongHitAnimation animation && NEUTRALIZE_ANIMATION_LIST.contains(animation);
    }
}