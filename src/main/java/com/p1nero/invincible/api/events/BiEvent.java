package com.p1nero.invincible.api.events;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.BiConsumer;

public class BiEvent {
    protected final BiConsumer<LivingEntityPatch<?>, Entity> event;

    public BiEvent(BiConsumer<LivingEntityPatch<?>, Entity> event) {
        this.event = event;
    }

    public static BiEvent createBiCommandEvent(String command, boolean isTarget) {
        BiConsumer<LivingEntityPatch<?>, Entity> event = (entityPatch, target) -> {
            Level server = entityPatch.getOriginal().level;
            CommandSourceStack css = entityPatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
            if (isTarget && target instanceof LivingEntity) {
                css = css.withEntity(target);
            }
            if (server.getServer() != null && entityPatch.getOriginal() != null) {
                server.getServer().getCommands().performPrefixedCommand(css, command);
            }
        };
        return new BiEvent(event);
    }

    public void testAndExecute(LivingEntityPatch<?> entityPatch, Entity target) {
        if (!entityPatch.isLogicalClient()) {
            this.event.accept(entityPatch, target);
        }
    }
}