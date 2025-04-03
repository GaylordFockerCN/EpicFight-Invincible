package com.p1nero.invincible.conditions;

import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.skill.ComboBasicAttack;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CooldownCondition implements Condition<ServerPlayerPatch> {

    private final boolean inCooldown;

    public CooldownCondition(boolean inCooldown){
        this.inCooldown = inCooldown;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        SkillDataManager manager = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getDataManager();
        if(manager.hasData(ComboBasicAttack.COOLDOWN_TIMER)){
            return this.inCooldown == manager.getDataValue(ComboBasicAttack.COOLDOWN_TIMER) > 0;
        }
        return false;
    }
}