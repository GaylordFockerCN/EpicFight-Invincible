package com.p1nero.invincible.conditions;

import com.p1nero.invincible.skill.ComboBasicAttack;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class UpCondition implements Condition<ServerPlayerPatch> {
    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        SkillDataManager dataManager = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getDataManager();
        return dataManager.hasData(ComboBasicAttack.UP) && dataManager.getDataValue(ComboBasicAttack.UP);
    }
}