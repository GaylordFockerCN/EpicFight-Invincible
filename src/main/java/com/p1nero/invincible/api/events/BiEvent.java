package com.p1nero.invincible.api.events;

import com.p1nero.invincible.api.Side;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.function.BiConsumer;

public record BiEvent(BiConsumer<PlayerPatch<?>, Entity> event, Side side) {
    public BiEvent(BiConsumer<PlayerPatch<?>, Entity> event) {
        this(event, Side.BOTH);
    }

    public static BiEvent createBiCommandEvent(String command, boolean isTarget) {
        BiConsumer<PlayerPatch<?>, Entity> event = (entityPatch, target) -> {
            Level server = entityPatch.getOriginal().level();
            CommandSourceStack css = entityPatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
            if (isTarget && target instanceof LivingEntity) {
                css = css.withEntity(target);
            }
            if (server.getServer() != null && entityPatch.getOriginal() != null) {
                server.getServer().getCommands().performPrefixedCommand(css, command);
            }
        };
        return new BiEvent(event, Side.SERVER);
    }

    public static BiEvent create(BiConsumer<PlayerPatch<?>, Entity> event, Side side) {
        return new BiEvent(event, side);
    }

    public static BiEvent createServerEvent(BiConsumer<PlayerPatch<?>, Entity> event) {
        return new BiEvent(event, Side.SERVER);
    }

    public static BiEvent createClientEvent(BiConsumer<PlayerPatch<?>, Entity> event) {
        return new BiEvent(event, Side.LOCAL_CLIENT);
    }

    public static BiEvent create(BiConsumer<PlayerPatch<?>, Entity> event) {
        return new BiEvent(event, Side.BOTH);
    }

    public void testAndExecute(PlayerPatch<?> entityPatch, Entity target) {
        if (side.test(entityPatch.getOriginal())) {
            this.event.accept(entityPatch, target);
        }
    }
}