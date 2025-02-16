package com.p1nero.invincible.conditions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * 检测某个buff的等级
 */
public class EffectCondition implements Condition<ServerPlayerPatch>{
    private final boolean isTarget;
    private final MobEffect effect;
    private final int amp;

    public EffectCondition(boolean isTarget, MobEffect effect, int amp) {
        this.isTarget = isTarget;
        this.effect = effect;
        this.amp = amp;
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
        if(livingEntity.hasEffect(effect)){
            return false;
        }
        return livingEntity.getEffect(effect).getAmplifier() == amp;
    }

}
