package com.p1nero.invincible.capability;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Consumer;

public class TimeStampedEvent implements Comparable<TimeStampedEvent> {
    private final float time;
    private final Consumer<LivingEntityPatch<?>> event;
    private boolean executed = false;

    public boolean isExecuted() {
        return executed;
    }
    public void resetExecuted(){
        executed = false;
    }

    public TimeStampedEvent(float time, Consumer<LivingEntityPatch<?>> event) {
        this.time = time;
        this.event = event;
    }

    public void testAndExecute(LivingEntityPatch<?> entityPatch, float prevElapsed, float elapsed) {
        if (this.time >= prevElapsed && this.time < elapsed && !entityPatch.isLogicalClient()) {
            this.event.accept(entityPatch);
            executed = true;
        }
    }

    public static TimeStampedEvent createTimeCommandEvent(float time, String command, boolean isTarget) {
        Consumer<LivingEntityPatch<?>> event = (entityPatch) -> {
            Level server = entityPatch.getOriginal().level();
            CommandSourceStack css = entityPatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
            if (isTarget && entityPatch.getTarget() != null) {
                css = css.withEntity(entityPatch.getTarget());
            }

            if (server.getServer() != null && entityPatch.getOriginal() != null) {
                server.getServer().getCommands().performPrefixedCommand(css, command);
            }

        };
        return new TimeStampedEvent(time, event);
    }

    @Override
    public int compareTo(@NotNull TimeStampedEvent event) {
        if (this.time == event.time) {
            return 0;
        } else {
            return this.time > event.time ? 1 : -1;
        }
    }
}
