package com.p1nero.invincible.conditions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.function.Supplier;

/**
 * 检测某个buff的等级
 */
public class MobEffectCondition implements Condition<ServerPlayerPatch>{
    private final boolean isTarget;
    private final Supplier<MobEffect> effectSupplier;
    private final int min, max;

    public MobEffectCondition(boolean isTarget, Supplier<MobEffect> effectSupplier, int min, int max) {
        this.isTarget = isTarget;
        this.effectSupplier = effectSupplier;
        this.min = min;
        this.max = max;
    }

    public MobEffectCondition(boolean isTarget, Supplier<MobEffect> effectSupplier, int level) {
        this.isTarget = isTarget;
        this.effectSupplier = effectSupplier;
        this.min = level;
        this.max = level;
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
        if(livingEntity.hasEffect(effectSupplier.get())){
            return false;
        }
        int level = livingEntity.getEffect(effectSupplier.get()).getAmplifier();
        return  level >= min && level <= max;
    }

}
