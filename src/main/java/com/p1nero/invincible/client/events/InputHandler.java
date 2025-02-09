package com.p1nero.invincible.client.events;

import com.p1nero.invincible.Config;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.client.keymappings.InvincibleKeyMappings;
import com.p1nero.invincible.skill.ComboBasicAttack;
import com.p1nero.invincible.skill.api.ComboNode;
import com.p1nero.invincible.skill.api.ComboType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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
import yesman.epicfight.skill.dodge.StepSkill;
import yesman.epicfight.world.entity.eventlistener.SkillExecuteEvent;

import java.util.*;

/**
 * 仅针对四个键的控制，写的一言难尽，能跑就行
 */
@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, value = Dist.CLIENT)
public class InputHandler {

    private static int reserveCounter, delayCounter;
    private static SkillSlot currentSlot;
    private static SkillSlot reservedSkillSlot;
    public static final Map<ComboType, KeyMapping> TYPE_KEY_MAP = new HashMap<>();
    public static final Map<KeyMapping, Boolean> KEY_STATE = new HashMap<>();
    public static final Queue<KeyMapping> INPUT_QUEUE = new ArrayDeque<>();

    static {
        TYPE_KEY_MAP.put(ComboNode.ComboTypes.KEY_1, InvincibleKeyMappings.KEY1);
        TYPE_KEY_MAP.put(ComboNode.ComboTypes.KEY_2, InvincibleKeyMappings.KEY2);
        TYPE_KEY_MAP.put(ComboNode.ComboTypes.KEY_3, InvincibleKeyMappings.KEY3);
        TYPE_KEY_MAP.put(ComboNode.ComboTypes.KEY_4, InvincibleKeyMappings.KEY4);
        for(KeyMapping keyMapping : TYPE_KEY_MAP.values()){
            KEY_STATE.put(keyMapping, false);
        }
    }

    /**
     * 用史诗战斗的那套可能会被顶掉所以自己写了预存
     * 同时给了按键输入一点小延迟，方便读取双键
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
                    tryRequestSkillExecute(currentSlot, true);
                }
            }
            if (reserveCounter > 0) {
                --reserveCounter;
                if(tryRequestSkillExecute(reservedSkillSlot, false)){
                    clearKeyReserve();
                    clearDelayKey();
                }
            }
        }

        if (INPUT_QUEUE.size() > 2) {
            KeyMapping keyMapping = INPUT_QUEUE.poll();
            if (!INPUT_QUEUE.contains(keyMapping)) {
                KEY_STATE.put(keyMapping, false);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (localPlayerPatch != null && Minecraft.getInstance().screen == null && !Minecraft.getInstance().isPaused()) {
            if (localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
                check(event.getButton(), event.getAction(), InvincibleKeyMappings.KEY1, InvincibleKeyMappings.KEY2, InvincibleKeyMappings.KEY3, InvincibleKeyMappings.KEY4);
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (localPlayerPatch != null && Minecraft.getInstance().screen == null && !Minecraft.getInstance().isPaused()) {
            if (event.getAction() == 1 && localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
                check(event.getKey(), event.getAction(), InvincibleKeyMappings.KEY1, InvincibleKeyMappings.KEY2, InvincibleKeyMappings.KEY3, InvincibleKeyMappings.KEY4);
            }
        }

    }

    public static void check(int key, int action, KeyMapping... keyMappings) {
        for (KeyMapping keyMapping : keyMappings) {
            if (action == 1 && key == keyMapping.getKey().getValue()) {
                INPUT_QUEUE.add(keyMapping);
                KEY_STATE.put(keyMapping, true);
                setDelay(SkillSlots.WEAPON_INNATE);
            }
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
     * 发起执行请求，并预存键位，战斗模式下才可以使用
     */
    public static boolean tryRequestSkillExecute(SkillSlot slot, boolean shouldReserve) {
        LocalPlayerPatch executor = ClientEngine.getInstance().getPlayerPatch();
        if (executor != null && executor.isBattleMode()) {
            if (sendExecuteRequest(executor, executor.getSkill(slot)).shouldReserverKey()) {
                if(shouldReserve){
                    setReserve(slot);
                }
                return false;
            } else {
                clearDelayKey();
                return true;
            }
        }
        return false;
    }

    public static SkillExecuteEvent sendExecuteRequest(LocalPlayerPatch executor, SkillContainer container) {
        ControllEngine controllEngine = ClientEngine.getInstance().controllEngine;
        SkillExecuteEvent event = new SkillExecuteEvent(executor, container);
        if (!container.canExecute(executor, event)) {
            if(container.getSkill() != null){
                container.getSkill().validationFeedback(executor);
            }
            return event;
        }
        executor.disableModelYRot(true);
        controllEngine.addPacketToSend(getExecutionPacket(container));
        return event;
    }

    public static Object getExecutionPacket(SkillContainer container) {
        CPExecuteSkill packet = new CPExecuteSkill(container.getSlotId());
        List<ComboType> typeList = new ArrayList<>(ComboType.ENUM_MANAGER.universalValues().stream().toList());
        typeList.sort(Comparator.comparingInt((comboType) -> -1 * comboType.getSubTypes().size()));//subType多的优先
        for(ComboType comboType : typeList){
            if(test(comboType)){
                packet.getBuffer().writeInt(comboType.universalOrdinal());
                break;
            }
        }
        return packet;
    }

    public static boolean test(ComboType comboType){
        if(comboType.getSubTypes().isEmpty()){
            return KEY_STATE.get(TYPE_KEY_MAP.get(comboType));
        } else {
            for(ComboType subType : comboType.getSubTypes()){
                if(!test(subType)){
                    return false;
                }
            }
            return true;
        }
    }

}
