package com.p1nero.invincible.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.types.AttackAnimation;

import java.util.*;

public class InvincibleEntity {
    private final Map<AttackAnimation.Phase, List<Entity>> phaseAttackTriedEntities = new HashMap<>();

    private final Set<AttackAnimation.Phase> usedPhases = new HashSet<>();

    public Map<AttackAnimation.Phase, List<Entity>> getPhaseAttackTriedEntities() {
        return phaseAttackTriedEntities;
    }

    public Set<AttackAnimation.Phase> getUsedPhases() {
        return usedPhases;
    }

    public List<Entity> getCurrentlyHurtEntities(AttackAnimation.Phase phase){
        List<Entity> toReturn = phaseAttackTriedEntities.get(phase);
        if(toReturn == null){
            List<Entity> newList = new ArrayList<>();
            phaseAttackTriedEntities.put(phase, newList);
            return newList;
        }
        return toReturn;
    }

    public void resetAttackPhaseCache(){
        phaseAttackTriedEntities.clear();
        usedPhases.clear();
    }

    public void setPhaseUsed(AttackAnimation.Phase phase) {
        usedPhases.add(phase);
    }

    public boolean isPhaseUsed(AttackAnimation.Phase phase) {
        return usedPhases.contains(phase);
    }

    public void saveNBTData(CompoundTag tag) {
    }

    public void loadNBTData(CompoundTag tag) {
    }

    public void tick() {
    }
}
