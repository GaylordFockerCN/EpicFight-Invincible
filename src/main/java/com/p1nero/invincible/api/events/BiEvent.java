package com.p1nero.invincible.api.events;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

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

    public enum Side {
        CLIENT((entity) -> entity.level().isClientSide),
        SERVER((entity) -> !entity.level().isClientSide),
        BOTH((entity) -> true),
        LOCAL_CLIENT((entity) -> {
            if (entity instanceof Player player) {
                return player.isLocalPlayer();
            } else {
                return false;
            }
        });

        public final Predicate<Entity> predicate;

        public boolean test(Entity entity) {
            return this.predicate.test(entity);
        }

        private Side(Predicate<Entity> predicate) {
            this.predicate = predicate;
        }
    }
}