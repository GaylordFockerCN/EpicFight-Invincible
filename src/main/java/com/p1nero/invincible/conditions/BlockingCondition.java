package com.p1nero.invincible.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class BlockingCondition implements Condition<ServerPlayerPatch> {

    public BlockingCondition() {

    }

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
        return serverPlayerPatch.getHoldingSkill() instanceof GuardSkill;

    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}