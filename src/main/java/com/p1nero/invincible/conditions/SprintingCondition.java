package com.p1nero.invincible.conditions;

import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class SprintingCondition implements Condition<ServerPlayerPatch> {
    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        ServerPlayer player = serverPlayerPatch.getOriginal();
        return player.isSprinting();
    }

}
