package com.p1nero.invincible.api.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class SprintingCondition implements Condition<ServerPlayerPatch> {

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
        ServerPlayer player = serverPlayerPatch.getOriginal();
        return player.isSprinting();
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
