package com.p1nero.invincible.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class SetStackCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invincible")
                .then(Commands.literal("setStackCount").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer())
                                .executes((context) -> {
                                    if(context.getSource().getPlayer() != null){
                                        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(context.getSource().getPlayer(), ServerPlayerPatch.class);
                                        serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).setStack(IntegerArgumentType.getInteger(context, "value"));
                                    }
                                    return 0;
                                })
                        )
                )
        );
    }
}
