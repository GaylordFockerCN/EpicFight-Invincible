package com.p1nero.invincible;

import com.mojang.logging.LogUtils;
import com.p1nero.invincible.attachment.InvincibleAttachments;
import com.p1nero.invincible.client.events.InputManager;
import com.p1nero.invincible.gameassets.InvincibleConditions;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.gameassets.InvincibleSkills;
import com.p1nero.invincible.item.InvincibleItems;
import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.api.skill.ComboType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(InvincibleMod.MOD_ID)
public class InvincibleMod {
    public static final String MOD_ID = "invincible";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InvincibleMod(IEventBus modEventBus, ModContainer modContainer) {
        InvincibleItems.ITEMS.register(modEventBus);
        InvincibleConditions.CONDITIONS.register(modEventBus);
        InvincibleSkillDataKeys.DATA_KEYS.register(modEventBus);
        InvincibleAttachments.ATTACHMENT_TYPES.register(modEventBus);
        InvincibleSkills.buildDatapackSkills();
        InvincibleSkills.REGISTRY.register(modEventBus);
        modEventBus.addListener(this::clientSetup);
        ComboType.ENUM_MANAGER.registerEnumCls(InvincibleMod.MOD_ID, ComboNode.ComboTypes.class);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void clientSetup(final FMLClientSetupEvent event){
        InputManager.init();
    }

}
