package com.p1nero.invincible;

import com.p1nero.invincible.client.events.InputManager;
import com.p1nero.invincible.gameassets.InvincibleSkills;
import com.p1nero.invincible.item.InvincibleItems;
import com.p1nero.invincible.skill.api.ComboNode;
import com.p1nero.invincible.skill.api.ComboType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(InvincibleMod.MOD_ID)
public class InvincibleMod {
    public static final String MOD_ID = "invincible";

    public InvincibleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        InvincibleItems.ITEMS.register(modEventBus);
        InvincibleSkills.registerSkills();
        ComboType.ENUM_MANAGER.loadPreemptive(ComboNode.ComboTypes.class);
        ComboType.initList();
        modEventBus.addListener(this::clientSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void clientSetup(final FMLClientSetupEvent event){
        InputManager.init();
    }

}
