package com.p1nero.invincible.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

public class EffectCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invincible")
                .then(Commands.literal("entityAfterImage").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("entity", EntityArgument.entities())
                                .executes((context) -> {
                                    for (Entity entity : EntityArgument.getEntities(context, "entity")) {
                                        EntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, EntityPatch.class);
                                        if (entityPatch != null) {
                                            context.getSource().getLevel().sendParticles(EpicFightParticles.ENTITY_AFTER_IMAGE.get(), entity.getX(), entity.getY(), entity.getZ(), 1, entity.getId(), 1, 1, entity.getId());
                                        }
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("groundSlam").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .then(Commands.argument("radius", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("noSound", BoolArgumentType.bool())
                                                .then(Commands.argument("noParticle", BoolArgumentType.bool())
                                                        .then(Commands.argument("hurtEntities", BoolArgumentType.bool())
                                                                .executes((context) -> {
                                                                    Entity entity = EntityArgument.getEntity(context, "entity");
                                                                    LevelUtil.circleSlamFracture(
                                                                            entity instanceof LivingEntity livingEntity ? livingEntity : null,
                                                                            entity.level(),
                                                                            entity.position().add(0, -1, 0),
                                                                            DoubleArgumentType.getDouble(context, "radius"),
                                                                            BoolArgumentType.getBool(context, "noSound"),
                                                                            BoolArgumentType.getBool(context, "noSound"),
                                                                            BoolArgumentType.getBool(context, "noSound"));
                                                                    return 0;
                                                                }))
                                                )
                                        )
                                )

                        )
                )
        );
    }
}
