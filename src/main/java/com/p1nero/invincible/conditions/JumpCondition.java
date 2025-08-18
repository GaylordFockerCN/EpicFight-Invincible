package com.p1nero.invincible.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.List;

public class JumpCondition implements Condition<PlayerPatch<?>> {
    
    @Override
    public Condition<PlayerPatch<?>> read(CompoundTag compoundTag) {
        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        return new CompoundTag();
    }

    @Override
    public boolean predicate(PlayerPatch<?> playerPatch) {
        Player player = playerPatch.getOriginal();
        return !player.onGround() && !player.isInWater() && player.getDeltaMovement().y > 0.05;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
