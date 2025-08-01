package com.p1nero.invincible.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class PressIntervalCondition implements Condition<ServerPlayerPatch> {

    private long min;
    private long max = Long.MAX_VALUE;
    public PressIntervalCondition(long min) {
        this.min = min;
    }

    public PressIntervalCondition(long minTicks, long maxTicks) {
        this.min = minTicks;
        this.max = maxTicks;
    }

    public PressIntervalCondition() {

    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        if (!compoundTag.contains("min") || !compoundTag.contains("max") ) {
            throw new IllegalArgumentException("custom condition error: 'min' or 'max' not specified!");
        }  else {
            this.min = compoundTag.getLong("min");
            this.max = compoundTag.getLong("max");
            return this;
        }
    }

    public long getMax() {
        return max;
    }

    public long getMin() {
        return min;
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putLong("min", min);
        compoundTag.putLong("max", max);
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
