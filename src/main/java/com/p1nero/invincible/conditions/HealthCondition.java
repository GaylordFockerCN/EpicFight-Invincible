package com.p1nero.invincible.conditions;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * 检测血量
 */
public class HealthCondition implements Condition<ServerPlayerPatch>{
    private final boolean isTarget, isLarger;
    private final float ratio;

    public HealthCondition(boolean isTarget, float ratio, boolean isLarger) {
        this.isTarget = isTarget;
        this.ratio = ratio;
        this.isLarger = isLarger;
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

    public boolean test(LivingEntity livingEntity){
        return isLarger == livingEntity.getHealth() / livingEntity.getMaxHealth() > ratio;
    }

}
