package com.p1nero.invincible.api.events;

import com.p1nero.invincible.api.Side;
import com.p1nero.invincible.attachment.InvinciblePlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.function.BiConsumer;

public record BaseEvent(BaseConsumer consumer, Side side) {
    public BaseEvent(BaseConsumer event) {
        this(event, Side.BOTH);
    }

    @Deprecated
    public BaseEvent(BiConsumer<PlayerPatch<?>, Entity> biConsumer) {
        this((playerPatch, target, invinciblePlayer) -> {
            biConsumer.accept(playerPatch, target);
        });
    }

    @Deprecated
    public BaseEvent(BiConsumer<PlayerPatch<?>, Entity> biConsumer, Side side) {
        this((playerPatch, target, invinciblePlayer) -> {
            biConsumer.accept(playerPatch, target);
        }, side);
    }

    public static BaseEvent createBiCommandEvent(String command, boolean isTarget) {
        BaseConsumer event = (entityPatch, target, invinciblePlayer) -> {
            Level server = entityPatch.getOriginal().level();
            CommandSourceStack css = entityPatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
            if (isTarget && target instanceof LivingEntity) {
                css = css.withEntity(target);
            }
            if (server.getServer() != null && entityPatch.getOriginal() != null) {
                server.getServer().getCommands().performPrefixedCommand(css, command);
            }
        };
        return new BaseEvent(event, Side.SERVER);
    }

    public static BaseEvent create(BaseConsumer event, Side side) {
        return new BaseEvent(event, side);
    }

    @Deprecated
    public static BaseEvent create(BiConsumer<PlayerPatch<?>, Entity> event, Side side) {
        return new BaseEvent(event, side);
    }

    public static BaseEvent createServerEvent(BaseConsumer event) {
        return new BaseEvent(event, Side.SERVER);
    }

    @Deprecated
    public static BaseEvent createServerEvent(BiConsumer<PlayerPatch<?>, Entity> event) {
        return new BaseEvent(event, Side.SERVER);
    }

    public static BaseEvent createClientEvent(BaseConsumer event) {
        return new BaseEvent(event, Side.LOCAL_CLIENT);
    }

    @Deprecated
    public static BaseEvent createClientEvent(BiConsumer<PlayerPatch<?>, Entity>  event) {
        return new BaseEvent(event, Side.LOCAL_CLIENT);
    }

    public static BaseEvent create(BaseConsumer event) {
        return new BaseEvent(event, Side.BOTH);
    }

    @Deprecated
    public static BaseEvent create(BiConsumer<PlayerPatch<?>, Entity>  event) {
        return new BaseEvent(event, Side.BOTH);
    }

    public void testAndExecute(PlayerPatch<?> entityPatch, Entity target, InvinciblePlayer invinciblePlayer) {
        if (side.test(entityPatch.getOriginal())) {
            this.consumer.accept(entityPatch, target, invinciblePlayer);
        }
    }
}