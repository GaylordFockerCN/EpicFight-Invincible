package com.p1nero.invincible.attachment;

import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.types.AttackAnimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvincibleEntity {
    private final Map<AttackAnimation.Phase, List<Entity>> phaseListMap = new HashMap<>();

    public Map<AttackAnimation.Phase, List<Entity>> getPhaseListMap() {
        return phaseListMap;
    }

    public List<Entity> getCurrentlyHurtEntities(AttackAnimation.Phase phase){
        List<Entity> toReturn = phaseListMap.get(phase);
        if(toReturn == null){
            List<Entity> newList = new ArrayList<>();
            phaseListMap.put(phase, newList);
            return newList;
        }
        return toReturn;
    }

    public void clearMap(){
        phaseListMap.clear();
    }

}
