package com.p1nero.invincible.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

/**
 * 作为默认条件
 */
public class TrueCondition implements Condition<ServerPlayerPatch> {
    
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
        return true;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
