package com.p1nero.invincible;

import com.mojang.logging.LogUtils;
import com.p1nero.invincible.client.events.InputManager;
import com.p1nero.invincible.gameassets.InvincibleConditions;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.item.InvincibleItems;
import com.p1nero.invincible.network.PacketHandler;
import com.p1nero.invincible.skill.api.ComboNode;
import com.p1nero.invincible.skill.api.ComboType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(InvincibleMod.MOD_ID)
public class InvincibleMod {
    public static final String MOD_ID = "invincible";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InvincibleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        InvincibleItems.ITEMS.register(modEventBus);
        InvincibleConditions.CONDITIONS.register(modEventBus);
        InvincibleSkillDataKeys.DATA_KEYS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        ComboType.ENUM_MANAGER.registerEnumCls(InvincibleMod.MOD_ID, ComboNode.ComboTypes.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event){
        PacketHandler.register();
    }
    private void clientSetup(final FMLClientSetupEvent event){
        InputManager.init();
    }

}
