package com.p1nero.invincible.command;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.skill.ComboBasicAttack;
import com.p1nero.invincible.skill.SimpleCustomInnateSkill;
import com.p1nero.invincible.skill.data.ComboJsonLoader;
import com.p1nero.invincible.skill.data.SkillJsonLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.Skill;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ReloadCommands {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
//        dispatcher.register(Commands.literal("invincible")
//                .then(Commands.literal("reload").requires((commandSourceStack) -> commandSourceStack.hasPermission(2) && commandSourceStack.getServer().isSingleplayer())
//                        .executes((context) -> {
//                            MappedRegistry<Skill> registry = (MappedRegistry<Skill>) EpicFightRegistries.SKILL;
//                            LOGGER.warn("[Invincible] : Unfreezing Skill Registry to reload custom skill. This should only happen in singleplayer world!");
//                            registry.unfreeze();
//                            reregisterJsonSkills(registry);
//                            reregisterJsonCombos(registry);
//                            if(context.getSource().getPlayer() != null) {
//                                context.getSource().getPlayer().displayClientMessage(Component.translatable("commands.invincible.reload"), false);
//                            }
//                            LOGGER.warn("[Invincible] : Skill reloaded. Refreezing Skill Registry.");
//                            registry.freeze();
//                            return 0;
//                        })
//                )
//        );
    }

    @ApiStatus.Internal
    public static void reregisterJsonCombos(MappedRegistry<Skill> registry) {
        Path invincibleCombos = FMLPaths.CONFIGDIR.get().resolve("invincible_combos");
        if(!Files.exists(invincibleCombos)){
            try {
                Files.createDirectory(invincibleCombos);

                return;
            } catch (IOException e){
                LOGGER.error("Failed to create invincible_combos folder!", e);
            }
        }
        try (Stream<Path> subDirs = Files.list(invincibleCombos)) {
            subDirs.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(comboFile -> {
                try {
                    InputStream inputStream = new FileInputStream(comboFile.toFile());
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    InputStreamReader reader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
                    JsonReader jsonReader = new JsonReader(reader);
                    jsonReader.setLenient(true);
                    JsonObject combo = Streams.parse(jsonReader).getAsJsonObject();
                    reader.close();
                    ComboBasicAttack.Builder skillBuilder = ComboJsonLoader.loadCombos(combo);
                    String skillName = combo.get("name").getAsString();
                    ResourceLocation key = ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, skillName);
                    ComboBasicAttack skill = skillBuilder.build(key);
                    CompoundTag params = new CompoundTag();
                    if (combo.has("consumption")) {
                        params.putFloat("consumption", combo.get("consumption").getAsFloat());
                    }
                    if (combo.has("max_stacks")) {
                        params.putInt("max_stacks", combo.get("max_stacks").getAsInt());
                    }
                    skill.loadDatapackParameters(params);
                    Registry.register(registry, key, skill);
                    LOGGER.info("LOAD ADDITIONAL SKILL >> {}", InvincibleMod.MOD_ID + ":" + skillName);
                } catch (IOException | CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("error when loading combos", e);
            throw new RuntimeException(e);
        }
    }

    @ApiStatus.Internal
    public static void reregisterJsonSkills(MappedRegistry<Skill> registry) {
        Path invincibleCombos = FMLPaths.CONFIGDIR.get().resolve("invincible_skills");
        if(!Files.exists(invincibleCombos)){
            try {
                Files.createDirectory(invincibleCombos);

                return;
            } catch (IOException e){
                LOGGER.error("Failed to create invincible_skills folder!", e);
            }
        }
        try (Stream<Path> subDirs = Files.list(invincibleCombos)) {
            subDirs.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(comboFile -> {
                try {
                    InputStream inputStream = new FileInputStream(comboFile.toFile());
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    InputStreamReader reader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
                    JsonReader jsonReader = new JsonReader(reader);
                    jsonReader.setLenient(true);
                    JsonObject combo = Streams.parse(jsonReader).getAsJsonObject();
                    reader.close();
                    SimpleCustomInnateSkill.Builder skillBuilder = SkillJsonLoader.loadSkill(combo);
                    String skillName = combo.get("name").getAsString();
                    ResourceLocation key = ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, skillName);
                    SimpleCustomInnateSkill skill = skillBuilder.build(key);
                    CompoundTag params = new CompoundTag();
                    if (combo.has("consumption")) {
                        params.putFloat("consumption", combo.get("consumption").getAsFloat());
                    }
                    if (combo.has("max_stacks")) {
                        params.putInt("max_stacks", combo.get("max_stacks").getAsInt());
                    }
                    skill.loadDatapackParameters(params);
                    Registry.register(registry, key, skill);
                    LOGGER.info("LOAD ADDITIONAL SKILL >> {}", InvincibleMod.MOD_ID + ":" + skillName);
                } catch (IOException | CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("error when loading skills", e);
            throw new RuntimeException(e);
        }
    }

}
