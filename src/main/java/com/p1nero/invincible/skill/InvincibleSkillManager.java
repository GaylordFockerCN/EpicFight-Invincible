package com.p1nero.invincible.skill;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.data.SkillJsonLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class InvincibleSkillManager {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static void buildDatapackSkills(SkillBuildEvent event) {
        Path invincibleCombos = FMLPaths.CONFIGDIR.get().resolve("invincible_combos");
        if(!Files.exists(invincibleCombos)){
            try {
                Files.createDirectory(invincibleCombos);
                return;
            } catch (IOException e){
                LOGGER.error("Failed to create default file!", e);
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
                    SkillBuildEvent.ModRegistryWorker registryWorker = event.createRegistryWorker(modId);
                    ComboBasicAttack skill = registryWorker.build(skillName, ComboBasicAttack::new, skillBuilder);
                    CompoundTag params = new CompoundTag();
                    if (combo.has("consumption")) {
                        params.putFloat("consumption", combo.get("consumption").getAsFloat());
                    }
                    if (combo.has("max_stacks")) {
                        params.putInt("max_stacks", combo.get("max_stacks").getAsInt());
                    }
                    skill.setParams(params);

                    LOGGER.info("LOAD ADDITIONAL SKILL >> {}", modId + ":" + skillName);
                } catch (IOException | CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("error when loading combos", e);
            throw new RuntimeException(e);
        }
    }

}
