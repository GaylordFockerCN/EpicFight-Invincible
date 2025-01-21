package com.p1nero.invincible.api.events;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Consumer;

public class Event {
    protected final Consumer<LivingEntityPatch<?>> event;

    public Event(Consumer<LivingEntityPatch<?>> event) {
        this.event = event;
    }

    public static Event CreateBiCommandEvent(String command) {
        Consumer<LivingEntityPatch<?>> event = (entityPatch) -> {
            Level server = entityPatch.getOriginal().level();
            CommandSourceStack css = entityPatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
            if (server.getServer() != null && entityPatch.getOriginal() != null) {
                server.getServer().getCommands().performPrefixedCommand(css, command);
            }
        };
        return new Event(event);
    }

    public void testAndExecute(LivingEntityPatch<?> entityPatch) {
        if (!entityPatch.isLogicalClient()) {
            this.event.accept(entityPatch);
        }
    }
}