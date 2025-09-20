package com.p1nero.invincible.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.skill.InvincibleSkillManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistry;
import org.slf4j.Logger;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;

public class ReloadCommands {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invincible")
                .then(Commands.literal("reload").requires((commandSourceStack) -> commandSourceStack.hasPermission(2) && commandSourceStack.getServer().isSingleplayer())
                        .executes((context) -> {
                            ForgeRegistry<Skill> registry = (ForgeRegistry<Skill>) SkillManager.getSkillRegistry();
                            LOGGER.warn("[Invincible] : Unfreezing Skill Registry to reload custom skill. This should only happen in singleplayer world!");
                            registry.unfreeze();
                            final SkillBuildEvent skillBuildEvnet = new SkillBuildEvent();
                            ModLoader.get().postEvent(skillBuildEvnet);
                            skillBuildEvnet.getAllSkills().forEach((skill) -> {
                                if(InvincibleSkillManager.MOD_ID_SET.contains(skill.getRegistryName().getNamespace())) {
                                    registry.register(skill.getRegistryName(), skill);
                                    if(context.getSource().getPlayer() != null) {
                                        context.getSource().getPlayer().displayClientMessage(Component.literal("Reloading skill: [" + skill.getRegistryName() + "]"), false);
                                    }
                                }
                            });
                            if(context.getSource().getPlayer() != null) {
                                context.getSource().getPlayer().displayClientMessage(Component.translatable("commands.invincible.reload"), false);
                            }
                            LOGGER.warn("[Invincible] : Skill reloaded. Refreezing Skill Registry.");
                            registry.freeze();
                            return 0;
                        })
                )
        );
    }

}
