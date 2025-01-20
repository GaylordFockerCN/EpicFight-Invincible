package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import yesman.epicfight.api.forgeevent.AnimationRegistryEvent;

public class InvincibleAnimations {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerAnimations(AnimationRegistryEvent event) {
        event.getRegistryMap().put(InvincibleMod.MOD_ID, InvincibleAnimations::build);

    }

    private static void build() {

    }

}
