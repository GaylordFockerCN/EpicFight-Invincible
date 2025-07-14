package com.p1nero.invincible.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class PressedTimeCondition implements Condition<ServerPlayerPatch> {

    private int min;
    private int max = Integer.MAX_VALUE;
    public PressedTimeCondition(int min) {
        this.min = min;
    }

    public PressedTimeCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }
    
    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        if (!compoundTag.contains("min") || !compoundTag.contains("max") ) {
            throw new IllegalArgumentException("custom condition error: 'min' or 'max' not specified!");
        }  else {
            this.min = compoundTag.getInt("min");
            this.max = compoundTag.getInt("max");
            return this;
        }
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("min", min);
        compoundTag.putInt("max", max);
        return compoundTag;
    }

    /**
     * 不走predicate，直接内部判断
     */
    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
       throw new IllegalCallerException();
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
