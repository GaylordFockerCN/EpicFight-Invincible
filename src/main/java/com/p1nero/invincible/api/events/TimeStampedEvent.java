package com.p1nero.invincible.api.events;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.function.Consumer;

public class TimeStampedEvent implements Comparable<TimeStampedEvent> {
    private final float time;
    private final Consumer<PlayerPatch<?>> event;
    private boolean executed = false;

    public boolean isExecuted() {
        return executed;
    }
    public void resetExecuted(){
        executed = false;
    }

    public TimeStampedEvent(float time, Consumer<PlayerPatch<?>> event) {
        this.time = time;
        this.event = event;
    }

    public void testAndExecute(PlayerPatch<?> entityPatch, float prevElapsed, float elapsed) {
        if (this.time >= prevElapsed && this.time < elapsed && !entityPatch.isLogicalClient()) {
            this.event.accept(entityPatch);
            executed = true;
        }
    }

    public static TimeStampedEvent createTimeCommandEvent(float time, String command, boolean isTarget) {
        Consumer<PlayerPatch<?>> event = (entityPatch) -> {
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
