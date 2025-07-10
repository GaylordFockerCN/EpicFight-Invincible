package com.p1nero.invincible.api.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class VehicleCondition implements Condition<ServerPlayerPatch> {
    private boolean hasVehicle;

    public VehicleCondition(){

    }

    public VehicleCondition(boolean hasVehicle){
        this.hasVehicle = hasVehicle;
    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        if (!compoundTag.contains("has_vehicle")) {
            throw new IllegalArgumentException("custom vehicle condition error: has_vehicle not specified!");
        }  else {
            this.hasVehicle = compoundTag.getBoolean("has_vehicle");
            return this;
        }
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("has_vehicle", hasVehicle);
        return tag;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        return hasVehicle == serverPlayerPatch.getOriginal().isPassenger();
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
