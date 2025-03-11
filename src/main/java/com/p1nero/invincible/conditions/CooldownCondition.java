package com.p1nero.invincible.conditions;

import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class CooldownCondition implements Condition<ServerPlayerPatch> {

    private boolean inCooldown;

    public CooldownCondition(boolean inCooldown){
        this.inCooldown = inCooldown;
    }

    public CooldownCondition(){
    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        if (!compoundTag.contains("in_cooldown")) {
            throw new IllegalArgumentException("custom cooldown condition error: in_cooldown not specified!");
        }  else {
            this.inCooldown = compoundTag.getBoolean("in_cooldown");
            return this;
        }
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("in_cooldown", inCooldown);
        return tag;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        SkillDataManager manager = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getDataManager();
        return this.inCooldown == manager.getDataValue(InvincibleSkillDataKeys.COOLDOWN.get()) > 0;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
