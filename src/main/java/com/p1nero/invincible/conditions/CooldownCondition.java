package com.p1nero.invincible.conditions;

import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CooldownCondition implements Condition<ServerPlayerPatch> {

    private final boolean inCooldown;

    public CooldownCondition(boolean inCooldown){
        this.inCooldown = inCooldown;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        return this.inCooldown == InvincibleCapabilityProvider.get(serverPlayerPatch.getOriginal()).getCooldown() > 0;
    }

}
