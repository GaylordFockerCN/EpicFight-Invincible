package com.p1nero.invincible.api.events;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.function.BiConsumer;

public class StunEvent extends BiEvent {
    private final int condition;

    private StunEvent(BiConsumer<LivingEntityPatch<?>, Entity> event, int condition) {
        super(event);
        this.condition = condition;
    }

    public static StunEvent CreateStunCommandEvent(String command, boolean isTarget, StunType stunType) {
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

        return new StunEvent(event, stunType.ordinal());
    }

    public void testAndExecute(LivingEntityPatch<?> entityPatch, Entity target, int condition) {
        if (!entityPatch.isLogicalClient() && this.condition == condition) {
            this.event.accept(entityPatch, target);
        }
    }
}