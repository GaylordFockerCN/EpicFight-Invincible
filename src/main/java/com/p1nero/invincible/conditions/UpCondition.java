package com.p1nero.invincible.conditions;

import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class UpCondition implements Condition<ServerPlayerPatch> {
    
    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        return new CompoundTag();
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        SkillDataManager dataManager = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getDataManager();
        return dataManager.hasData(InvincibleSkillDataKeys.UP.get()) && dataManager.getDataValue(InvincibleSkillDataKeys.UP.get());
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
