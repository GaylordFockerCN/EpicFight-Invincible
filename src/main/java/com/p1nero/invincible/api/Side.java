package com.p1nero.invincible.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

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

        Side(Predicate<Entity> predicate) {
            this.predicate = predicate;
        }

    }