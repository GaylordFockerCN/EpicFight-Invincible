package com.p1nero.invincible.conditions;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public abstract class CustomTargetCondition implements Condition<ServerPlayerPatch> {
    private final boolean isTarget;

    protected CustomTargetCondition(boolean isTarget) {
        this.isTarget = isTarget;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        if(isTarget){
            if(serverPlayerPatch.getTarget() == null){
                return false;
            }
            return test(serverPlayerPatch.getTarget());
        } else {
            return test(serverPlayerPatch.getOriginal());
        }
    }

    public abstract boolean test(LivingEntity livingEntity);

}
