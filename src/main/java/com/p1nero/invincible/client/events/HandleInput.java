package com.p1nero.invincible.client.events;

import com.p1nero.invincible.Config;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.client.keymappings.InvincibleKeyMappings;
import com.p1nero.invincible.mixin.ControlEngineAccessor;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, value = Dist.CLIENT)
public class HandleInput {

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if(localPlayerPatch != null){
            if(event.getAction() == 1 && localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack){
                check(event.getButton(), InvincibleKeyMappings.KEY1);
                check(event.getButton(), InvincibleKeyMappings.KEY2);
                check(event.getButton(), InvincibleKeyMappings.KEY3);
                check(event.getButton(), InvincibleKeyMappings.KEY4);
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event){
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if(localPlayerPatch != null){
            if(event.getAction() == 1 && localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack){
                check(event.getKey(), InvincibleKeyMappings.KEY1);
                check(event.getKey(), InvincibleKeyMappings.KEY2);
                check(event.getKey(), InvincibleKeyMappings.KEY3);
                check(event.getKey(), InvincibleKeyMappings.KEY4);
            }
        }

    }

    public static void check(int key, KeyMapping keyMapping){
        if(key == keyMapping.getKey().getValue()){
            sendSkillPacket(SkillSlots.WEAPON_INNATE, keyMapping);
        }
    }

    /**
     * 发起执行请求，并预存键位
     */
    public static void sendSkillPacket(SkillSlot slot, KeyMapping key){
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null){
            LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getEntityPatch(player, LocalPlayerPatch.class);
            if(localPlayerPatch.getSkill(slot).sendExecuteRequest(localPlayerPatch, ClientEngine.getInstance().controllEngine).shouldReserverKey()){
                ControlEngineAccessor controlEngine = (ControlEngineAccessor) ClientEngine.getInstance().controllEngine;
                controlEngine.setReserveCounter(Config.RESERVE_TICK.get());
                controlEngine.setReservedOrChargingSkillSlot(slot);
                controlEngine.setReservedKey(key);
            }
        }
    }
}
