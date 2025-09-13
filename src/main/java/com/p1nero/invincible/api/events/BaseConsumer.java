package com.p1nero.invincible.api.events;

import com.p1nero.invincible.attachment.InvinciblePlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@FunctionalInterface
public interface BaseConsumer {
    void accept(PlayerPatch<?> playerPatch, @Nullable Entity target, InvinciblePlayer invinciblePlayer);
}