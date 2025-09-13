package com.p1nero.invincible.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.p1nero.invincible.attachment.InvincibleAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class SetPlayerStateCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invincible")
                .then(Commands.literal("setPlayerPhase").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer())
                                .executes((context) -> {
                                    if (context.getSource().getPlayer() != null) {
                                        InvincibleAttachments.getPlayer(context.getSource().getPlayer()).setPhase(IntegerArgumentType.getInteger(context, "value"));
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("resetPhase").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .executes((context) -> {
                            if (context.getSource().getPlayer() != null) {
                                InvincibleAttachments.getPlayer(context.getSource().getPlayer()).resetPhase();
                            }
                            return 0;
                        })
                )
                .then(Commands.literal("setStack").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer())
                                .executes((context) -> {
                                    if (context.getSource().getPlayer() != null) {
                                        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(context.getSource().getPlayer(), ServerPlayerPatch.class);
                                        serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill().setStackSynchronize(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), IntegerArgumentType.getInteger(context, "value"));
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("consumeStack").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer())
                                .executes((context) -> {
                                    if (context.getSource().getPlayer() != null) {
                                        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(context.getSource().getPlayer(), ServerPlayerPatch.class);
                                        SkillContainer weaponInnate = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE);
                                        weaponInnate.getSkill().setStackSynchronize(weaponInnate, Math.max(0, weaponInnate.getStack() - IntegerArgumentType.getInteger(context, "value")));
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("setConsumption").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("value", FloatArgumentType.floatArg())
                                .executes((context) -> {
                                    if (context.getSource().getPlayer() != null) {
                                        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(context.getSource().getPlayer(), ServerPlayerPatch.class);
                                        serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill().setConsumptionSynchronize(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), FloatArgumentType.getFloat(context, "value"));
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("consumeConsumption").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("value", FloatArgumentType.floatArg())
                                .executes((context) -> {
                                    if (context.getSource().getPlayer() != null) {
                                        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(context.getSource().getPlayer(), ServerPlayerPatch.class);
                                        SkillContainer weaponInnate = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE);
                                        weaponInnate.getSkill().setConsumptionSynchronize(weaponInnate, Math.max(0, weaponInnate.getResource() - FloatArgumentType.getFloat(context, "value")));
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("consumeStamina").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("value", FloatArgumentType.floatArg())
                                .executes((context) -> {
                                    if (context.getSource().getPlayer() != null) {
                                        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(context.getSource().getPlayer(), ServerPlayerPatch.class);
                                        serverPlayerPatch.setStamina(Math.max(0, serverPlayerPatch.getStamina() - FloatArgumentType.getFloat(context, "value")));
                                        SkillContainer weaponInnate = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE);
                                        weaponInnate.getSkill().setConsumptionSynchronize(weaponInnate, Math.max(0, weaponInnate.getResource() - FloatArgumentType.getFloat(context, "value")));
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("setStamina").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("value", FloatArgumentType.floatArg())
                                .executes((context) -> {
                                    if (context.getSource().getPlayer() != null) {
                                        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(context.getSource().getPlayer(), ServerPlayerPatch.class);
                                        serverPlayerPatch.setStamina(FloatArgumentType.getFloat(context, "value"));
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.argument("players", EntityArgument.players()).requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("setPlayerPhase").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes((context) -> {
                                            for (Player player : EntityArgument.getPlayers(context, "players")) {
                                                InvincibleAttachments.getPlayer(player).setPhase(IntegerArgumentType.getInteger(context, "value"));
                                            }
                                            return 0;
                                        })
                                )
                        )
                        .then(Commands.literal("resetPhase").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                                .executes((context) -> {
                                    for (Player player : EntityArgument.getPlayers(context, "players")) {
                                        InvincibleAttachments.getPlayer(player).resetPhase();
                                    }
                                    return 0;
                                })
                        )
                        .then(Commands.literal("setStack").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes((context) -> {
                                            for (Player player : EntityArgument.getPlayers(context, "players")) {
                                                ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
                                                serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill().setStackSynchronize(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), IntegerArgumentType.getInteger(context, "value"));
                                            }
                                            return 0;
                                        })
                                )
                        )
                        .then(Commands.literal("consumeStack").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes((context) -> {
                                            for (Player player : EntityArgument.getPlayers(context, "players")) {
                                                ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
                                                SkillContainer weaponInnate = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE);
                                                weaponInnate.getSkill().setStackSynchronize(weaponInnate, Math.max(0, weaponInnate.getStack() - IntegerArgumentType.getInteger(context, "value")));
                                            }
                                            return 0;
                                        })
                                )
                        )
                        .then(Commands.literal("setConsumption").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                                .then(Commands.argument("value", FloatArgumentType.floatArg())
                                        .executes((context) -> {
                                            for (Player player : EntityArgument.getPlayers(context, "players")) {
                                                ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
                                                serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill().setConsumptionSynchronize(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), FloatArgumentType.getFloat(context, "value"));
                                            }
                                            return 0;
                                        })
                                )
                        )
                        .then(Commands.literal("consumeConsumption").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                                .then(Commands.argument("value", FloatArgumentType.floatArg())
                                        .executes((context) -> {
                                            for (Player player : EntityArgument.getPlayers(context, "players")) {
                                                ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
                                                SkillContainer weaponInnate = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE);
                                                weaponInnate.getSkill().setConsumptionSynchronize(weaponInnate, Math.max(0, weaponInnate.getResource() - FloatArgumentType.getFloat(context, "value")));
                                            }
                                            return 0;
                                        })
                                )
                        )
                        .then(Commands.literal("consumeStamina").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                                .then(Commands.argument("value", FloatArgumentType.floatArg())
                                        .executes((context) -> {
                                            for (Player player : EntityArgument.getPlayers(context, "players")) {
                                                ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
                                                serverPlayerPatch.setStamina(Math.max(0, serverPlayerPatch.getStamina() - FloatArgumentType.getFloat(context, "value")));
                                                SkillContainer weaponInnate = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE);
                                                weaponInnate.getSkill().setConsumptionSynchronize(weaponInnate, Math.max(0, weaponInnate.getResource() - FloatArgumentType.getFloat(context, "value")));
                                            }
                                            return 0;
                                        })
                                )
                        )
                        .then(Commands.literal("setStamina").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                                .then(Commands.argument("value", FloatArgumentType.floatArg())
                                        .executes((context) -> {
                                            for (Player player : EntityArgument.getPlayers(context, "players")) {
                                                ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
                                                serverPlayerPatch.setStamina(FloatArgumentType.getFloat(context, "value"));
                                            }
                                            return 0;
                                        })
                                )
                        )
                )
        );
    }

}
