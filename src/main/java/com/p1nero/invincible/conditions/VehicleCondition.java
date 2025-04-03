package com.p1nero.invincible.conditions;

import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class VehicleCondition implements Condition<ServerPlayerPatch> {
    private final boolean hasVehicle;
    public VehicleCondition(boolean hasVehicle){
        this.hasVehicle = hasVehicle;
    }
    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        return hasVehicle == serverPlayerPatch.getOriginal().isPassenger();
    }
}