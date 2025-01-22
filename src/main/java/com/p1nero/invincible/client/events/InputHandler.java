package com.p1nero.invincible.client.events;

import com.google.common.collect.Maps;
import com.p1nero.invincible.Config;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.client.keymappings.InvincibleKeyMappings;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.client.CPExecuteSkill;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.entity.eventlistener.SkillExecuteEvent;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 仅针对四个键的控制，写的一言难尽，能跑就行
 */
@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, value = Dist.CLIENT)
public class InputHandler {

    private static int reserveCounter, delayCounter;
    private static SkillSlot currentSlot;
    private static SkillSlot reservedSkillSlot;
    private static final Map<KeyMapping, Boolean> KEY_STATE = new HashMap<>();
    private static final Queue<KeyMapping> INPUT_QUEUE = new ArrayDeque<>();

    static {
        KEY_STATE.put(InvincibleKeyMappings.KEY1, false);
        KEY_STATE.put(InvincibleKeyMappings.KEY2, false);
        KEY_STATE.put(InvincibleKeyMappings.KEY3, false);
        KEY_STATE.put(InvincibleKeyMappings.KEY4, false);
    }

    /**
     * 用史诗战斗的那套可能会被顶掉
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null) {
            if (delayCounter > 0) {
                delayCounter--;
                clearKeyReserve();
                if (delayCounter == 0) {
                    delayCounter = -1;
                    tryRequestSkillExecute(currentSlot);
                }
            }
            if (reserveCounter > 0) {
                --reserveCounter;
                SkillContainer skill = playerPatch.getSkill(reservedSkillSlot);
                if (skill.getSkill() != null && skill.sendExecuteRequest(playerPatch, ClientEngine.getInstance().controllEngine).isExecutable()) {
                    clearKeyReserve();
                    clearDelayKey();
                }
            }
        };
        if (INPUT_QUEUE.size() > 2) {
            KeyMapping keyMapping = INPUT_QUEUE.poll();
            if(!INPUT_QUEUE.contains(keyMapping)){
                KEY_STATE.put(keyMapping, false);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (localPlayerPatch != null) {
            if (localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
                check(event.getButton(), InvincibleKeyMappings.KEY1, event.getAction());
                check(event.getButton(), InvincibleKeyMappings.KEY2, event.getAction());
                check(event.getButton(), InvincibleKeyMappings.KEY3, event.getAction());
                check(event.getButton(), InvincibleKeyMappings.KEY4, event.getAction());
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (localPlayerPatch != null) {
            if (event.getAction() == 1 && localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
                check(event.getKey(), InvincibleKeyMappings.KEY1, event.getAction());
                check(event.getKey(), InvincibleKeyMappings.KEY2, event.getAction());
                check(event.getKey(), InvincibleKeyMappings.KEY3, event.getAction());
                check(event.getKey(), InvincibleKeyMappings.KEY4, event.getAction());
            }
        }

    }

    public static void check(int key, KeyMapping keyMapping, int action) {
        if (action == 1 && key == keyMapping.getKey().getValue()) {
            INPUT_QUEUE.add(keyMapping);
            KEY_STATE.put(keyMapping, true);
            setDelay(SkillSlots.WEAPON_INNATE);
        }
    }

    /**
     * 清理存下的按键，成功执行才清除
     */
    public static void clearDelayKey() {
        KEY_STATE.forEach(((keyMapping, aBoolean) -> KEY_STATE.put(keyMapping, false)));
    }

    public static void clearKeyReserve() {
        reserveCounter = -1;
        reservedSkillSlot = null;
    }

    /**
     * 延迟输入计时器，影响双键按下
     */
    public static void setDelayCounter(int delayCounter) {
        InputHandler.delayCounter = delayCounter;
    }

    public static void setDelay(SkillSlot slot) {
        //不能被顶掉
        if (InputHandler.delayCounter <= 0) {
            InputHandler.delayCounter = Config.INPUT_DELAY_TICK.get();
            InputHandler.currentSlot = slot;
        }
    }

    /**
     * 延迟输入的技能栏
     */
    public static void setCurrentSlot(SkillSlot currentSlot) {
        InputHandler.currentSlot = currentSlot;
    }

    /**
     * 预存计时器
     */
    public static void setReserveCounter(int reserveCounter) {
        InputHandler.reserveCounter = reserveCounter;
    }

    public static void setReserve(SkillSlot reserve) {
        InputHandler.reserveCounter = Config.RESERVE_TICK.get();
        InputHandler.reservedSkillSlot = reserve;
    }

    /**
     * 预存的技能栏
     */
    public static void setReservedSkillSlot(SkillSlot reservedSkillSlot) {
        InputHandler.reservedSkillSlot = reservedSkillSlot;
    }

    /**
     * 发起执行请求，并预存键位
     */
    public static void tryRequestSkillExecute(SkillSlot slot) {
        LocalPlayerPatch executor = ClientEngine.getInstance().getPlayerPatch();
        if (executor != null) {
            if (sendExecuteRequest(executor, executor.getSkill(slot)).shouldReserverKey()) {
                setReserve(slot);
            } else {
                clearDelayKey();
            }
        }
    }

    public static SkillExecuteEvent sendExecuteRequest(LocalPlayerPatch executor, SkillContainer container) {
        ControllEngine controllEngine = ClientEngine.getInstance().controllEngine;
        SkillExecuteEvent event = new SkillExecuteEvent(executor, container);
        if (!container.canExecute(executor, event)) {
            container.getSkill().validationFeedback(executor);
            return event;
        }
        executor.disableModelYRot(true);
        controllEngine.addPacketToSend(getExecutionPacket(container));
        return event;
    }

    public static Object getExecutionPacket(SkillContainer container) {
        CPExecuteSkill packet = new CPExecuteSkill(container.getSlotId());
        packet.getBuffer().writeBoolean(KEY_STATE.get(InvincibleKeyMappings.KEY1));
        packet.getBuffer().writeBoolean(KEY_STATE.get(InvincibleKeyMappings.KEY2));
        packet.getBuffer().writeBoolean(KEY_STATE.get(InvincibleKeyMappings.KEY3));
        packet.getBuffer().writeBoolean(KEY_STATE.get(InvincibleKeyMappings.KEY4));
        return packet;
    }

}
