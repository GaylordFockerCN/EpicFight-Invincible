package com.p1nero.invincible;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.p1nero.invincible.client.events.InputManager;
import com.p1nero.invincible.data.SkillJsonLoader;
import com.p1nero.invincible.gameassets.InvincibleConditions;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.item.InvincibleItems;
import com.p1nero.invincible.network.PacketHandler;
import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.api.skill.ComboType;
import com.p1nero.invincible.skill.ComboBasicAttack;
import com.p1nero.invincible.skill.InvincibleSkillManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Mod(InvincibleMod.MOD_ID)
public class InvincibleMod {
    public static final String MOD_ID = "invincible";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InvincibleMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        InvincibleItems.ITEMS.register(modEventBus);
        InvincibleConditions.CONDITIONS.register(modEventBus);
        InvincibleSkillDataKeys.DATA_KEYS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(InvincibleSkillManager::buildDatapackSkills);
        ComboType.ENUM_MANAGER.registerEnumCls(InvincibleMod.MOD_ID, ComboNode.ComboTypes.class);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event){
        PacketHandler.register();
    }
    private void clientSetup(final FMLClientSetupEvent event){
        InputManager.init();
    }

}
