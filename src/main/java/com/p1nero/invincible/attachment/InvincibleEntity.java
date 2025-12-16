package com.p1nero.invincible.attachment;

import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.types.AttackAnimation;

import java.util.*;

public class InvincibleEntity {
    private final Map<AttackAnimation.Phase, List<Entity>> phaseAttackTriedEntities = new HashMap<>();
    private final Set<AttackAnimation.Phase> usedPhases = new HashSet<>();

    public Map<AttackAnimation.Phase, List<Entity>> getPhaseAttackTriedEntities() {
        return phaseAttackTriedEntities;
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

    public void clearMap(){
        phaseAttackTriedEntities.clear();
        usedPhases.clear();
    }

    public void setPhaseUsed(AttackAnimation.Phase phase) {
        usedPhases.add(phase);
    }

    public boolean isPhaseUsed(AttackAnimation.Phase phase) {
        return usedPhases.contains(phase);
    }

}
