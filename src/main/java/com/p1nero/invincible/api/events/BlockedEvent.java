package com.p1nero.invincible.api.events;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.BiConsumer;

public class BlockedEvent {
    protected final BiConsumer<LivingEntityPatch<?>, Entity> event;
    boolean isParry;

    public BlockedEvent(BiConsumer<LivingEntityPatch<?>, Entity> event, boolean isParry) {
        this.event = event;
        this.isParry = isParry;
    }

    public static BlockedEvent CreateBlockCommandEvent(String command, boolean isTarget, boolean isParry) {
        BiConsumer<LivingEntityPatch<?>, Entity> event = (entityPatch, target) -> {
            Level server = entityPatch.getOriginal().level();
            CommandSourceStack css = entityPatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
            if (isTarget && target instanceof LivingEntity) {
                css = css.withEntity(target);
            }
            if (server.getServer() != null && entityPatch.getOriginal() != null) {
                server.getServer().getCommands().performPrefixedCommand(css, command);
            }
        };
        return new BlockedEvent(event, isParry);
    }

    public void testAndExecute(LivingEntityPatch<?> entityPatch, Entity target, boolean isParry) {
        if (!entityPatch.isLogicalClient() && this.isParry == isParry) {
            this.event.accept(entityPatch, target);
        }
    }
}