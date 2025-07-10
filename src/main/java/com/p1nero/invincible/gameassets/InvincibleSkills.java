package com.p1nero.invincible.gameassets;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.skill.data.SkillJsonLoader;
import com.p1nero.invincible.gameassets.combos.ComboDemo;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.entries.EpicFightConditions;
import yesman.epicfight.skill.Skill;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 注册技能，然后在{@link InvincibleWeaponCapabilityPresets}中使用
 * 预设的Condition可以参考 {@link EpicFightConditions} 和 {@link InvincibleConditions}
 */
public class InvincibleSkills {
    public static final DeferredRegister<Skill> REGISTRY = DeferredRegister.create(EpicFightRegistries.Keys.SKILL, InvincibleMod.MOD_ID);
    public static final DeferredHolder<Skill, ComboBasicAttack> COMBO_DEMO = REGISTRY.register("combo_attacks",
            (key) -> ComboBasicAttack.createComboBasicAttack(ComboBasicAttack::new).setCombo(ComboDemo.demo()).setShouldDrawGui(true).build(key, ComboBasicAttack.class));

    public static void buildDatapackSkills() {
        Path invincibleCombos = FMLPaths.CONFIGDIR.get().resolve("invincible_combos");
        if(!Files.exists(invincibleCombos)){
            try {
                Files.createDirectory(invincibleCombos);
                return;
            } catch (IOException e){
                InvincibleMod.LOGGER.error("Failed to create default file!", e);
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
                    ComboBasicAttack.Builder skillBuilder = SkillJsonLoader.loadSkill(combo);
                    String modId = combo.get("mod_id").getAsString();
                    String skillName = combo.get("name").getAsString();
                    REGISTRY.register(skillName, (key) -> {
                        ComboBasicAttack skill = skillBuilder.build(key, ComboBasicAttack.class);
                        CompoundTag params = new CompoundTag();
                        if (combo.has("consumption")) {
                            params.putFloat("consumption", combo.get("consumption").getAsFloat());
                        }
                        if (combo.has("max_stacks")) {
                            params.putInt("max_stacks", combo.get("max_stacks").getAsInt());
                        }
                        skill.loadDatapackParameters(params);
                        return skill;
                    });

                    InvincibleMod.LOGGER.info("LOAD ADDITIONAL SKILL >> {}", modId + ":" + skillName);
                } catch (IOException | CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            InvincibleMod.LOGGER.error("error when loading combos", e);
            throw new RuntimeException(e);
        }
    }

}
