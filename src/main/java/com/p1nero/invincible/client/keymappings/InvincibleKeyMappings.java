package com.p1nero.invincible.client.keymappings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.client.input.CombatKeyMapping;

/**
 * 提供四个预设的键
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InvincibleKeyMappings {
    public static final KeyMapping KEY1 = new CombatKeyMapping("key.invincible.key1", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_1, "key.invincible.category");
    public static final KeyMapping KEY2 = new CombatKeyMapping("key.invincible.key2", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_2, "key.invincible.category");
    public static final KeyMapping KEY3 = new CombatKeyMapping("key.invincible.key3", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, "key.invincible.category");
    public static final KeyMapping KEY4 = new CombatKeyMapping("key.invincible.key4", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, "key.invincible.category");

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY1);
        event.register(KEY2);
        event.register(KEY3);
        event.register(KEY4);
    }

}
