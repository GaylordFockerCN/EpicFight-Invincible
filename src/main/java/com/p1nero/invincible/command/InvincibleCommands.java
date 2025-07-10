package com.p1nero.invincible.command;

import com.mojang.brigadier.CommandDispatcher;
import com.p1nero.invincible.InvincibleMod;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = InvincibleMod.MOD_ID)
public class InvincibleCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        SetPlayerStateCommands.register(dispatcher);
        EffectCommands.register(dispatcher);
    }
}