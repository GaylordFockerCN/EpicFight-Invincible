package com.p1nero.invincible.api.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class StackCondition implements Condition<ServerPlayerPatch> {

    private int min, max;

    public StackCondition(int min, int max){
        this.min = min;
        this.max = max;
    }

    public StackCondition(){
        this.min = 1;
        this.max = 1;
    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        if (!compoundTag.contains("min") && !compoundTag.contains("max")) {
            throw new IllegalArgumentException("custom player stack condition error: min or max not specified!");
        }  else {
            this.min = compoundTag.getInt("min");
            this.max = compoundTag.getInt("max");
            return this;
        }
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("min", this.min);
        tag.putInt("max", this.max);
        return tag;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        int stack = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getStack();
        return stack >= min && stack <= max;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
