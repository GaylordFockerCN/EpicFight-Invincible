package com.p1nero.invincible.conditions;

import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class JumpCondition implements Condition<ServerPlayerPatch> {

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        ServerPlayer player = serverPlayerPatch.getOriginal();
        return !player.isOnGround() && !player.isInWater() && player.getDeltaMovement().y > 0.05;//其实客户端判断更准点
    }
}