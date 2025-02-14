package com.p1nero.invincible.command;

import com.mojang.brigadier.CommandDispatcher;
import com.p1nero.invincible.InvincibleMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InvincibleCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        SetPlayerStateCommands.register(dispatcher);
        EffectCommands.register(dispatcher);
    }
}