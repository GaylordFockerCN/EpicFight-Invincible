package com.p1nero.invincible.conditions;

import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class StackCondition implements Condition<ServerPlayerPatch> {

    private final int min;
    private final int max;

    public StackCondition(int min, int max){
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        int stack = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getStack();
        return stack >= min && stack <= max;
    }

}
