package com.p1nero.invincible.conditions;

import com.p1nero.invincible.skill.ComboBasicAttack;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class DodgeSuccessCondition implements Condition<ServerPlayerPatch> {

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        SkillDataManager manager = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getDataManager();
        if(manager.hasData(ComboBasicAttack.DODGE_SUCCESS_TIMER)){
            return manager.getDataValue(ComboBasicAttack.DODGE_SUCCESS_TIMER) > 0;
        }
        return false;
    }

}
