package com.p1nero.invincible.capability;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import yesman.epicfight.api.animation.types.AttackAnimation;

import java.util.*;

public class InvincibleEntity {

    public static final Logger LOGGER = LogUtils.getLogger();

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

    public InvincibleEntity err() {
        LOGGER.error("Error! Create a new InvincibleEntity!", new Exception());
        return this;
    }

}
