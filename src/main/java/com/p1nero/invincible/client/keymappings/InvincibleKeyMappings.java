package com.p1nero.invincible.client.keymappings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.client.input.CombatKeyMapping;

/**
 * 提供四个预设的键
 */
@EventBusSubscriber(value = Dist.CLIENT)
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

    public static Component getName(KeyMapping keyMapping) {
        return keyMapping.getTranslatedKeyMessage();
    }

    public static Component getTranslatableKey1(){
        return getName(KEY1);
    }
    public static Component getTranslatableKey2(){
        return getName(KEY2);
    }
    public static Component getTranslatableKey3(){
        return getName(KEY3);
    }
    public static Component getTranslatableKey4(){
        return getName(KEY4);
    }

}
