package com.p1nero.invincible.conditions;

import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class PlayerPhaseCondition implements Condition<ServerPlayerPatch> {

    private final int min;
    private final int max;

    public PlayerPhaseCondition(int min, int max){
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        int phase = InvincibleCapabilityProvider.get(serverPlayerPatch.getOriginal()).getPhase();
        return phase >= min && phase <= max;
    }
}