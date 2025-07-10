package com.p1nero.invincible.client.events;

import com.p1nero.invincible.Config;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.client.keymappings.InvincibleKeyMappings;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.skill.ComboBasicAttack;
import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.api.skill.ComboType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import yesman.epicfight.api.neoforgeevent.playerpatch.SkillExecuteEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPExecuteSkill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import javax.annotation.Nullable;
import java.util.*;

@EventBusSubscriber(modid = InvincibleMod.MOD_ID, value = Dist.CLIENT)
public class InputManager {

    private static int reserveCounter, delayCounter;
    private static SkillSlot currentSlot;
    private static SkillSlot reservedSkillSlot;
    private static final Map<ComboType, KeyMapping> TYPE_KEY_MAP = new HashMap<>();
    private static final Map<KeyMapping, Boolean> KEY_STATE_CACHE = new HashMap<>();
    private static final Queue<KeyMapping> INPUT_QUEUE = new ArrayDeque<>();

    /**
     * 绑定模组自带的的按键
     */
    public static void init(){
        register(ComboNode.ComboTypes.KEY_1, InvincibleKeyMappings.KEY1);
        register(ComboNode.ComboTypes.KEY_2, InvincibleKeyMappings.KEY2);
        register(ComboNode.ComboTypes.KEY_3, InvincibleKeyMappings.KEY3);
        register(ComboNode.ComboTypes.KEY_4, InvincibleKeyMappings.KEY4);
        register(ComboNode.ComboTypes.DODGE, EpicFightKeyMappings.DODGE);
        register(ComboNode.ComboTypes.WEAPON_INNATE, EpicFightKeyMappings.WEAPON_INNATE_SKILL);
    }

    /**
     * 自定义按键的注册
     */
    public static void register(ComboType type, KeyMapping keyMapping){
        TYPE_KEY_MAP.put(type, keyMapping);
        KEY_STATE_CACHE.put(keyMapping, false);
    }

    /**
     * 用史诗战斗的那套可能会被顶掉所以自己写了预存
     * 同时给了按键输入一点小延迟，方便读取双键
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null) {
            //延迟输入的判断
            if (delayCounter > 0) {
                delayCounter--;
                clearReservedKeys();
                if (delayCounter == 0) {
                    delayCounter = -1;
                    tryRequestSkillExecute(currentSlot, true);
                }
            }
            //缓存的按键的处理
            if (reserveCounter > 0) {
                --reserveCounter;
                if(tryRequestSkillExecute(reservedSkillSlot, false)){
                    clearReservedKeys();
                    clearKeyCache();
                }
                if(reserveCounter == 0){
                    clearReservedKeys();
                    clearKeyCache();
                }
            }

            //判断asdw是否按下，用于Condition判断。
            if(playerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack){
                Options options = Minecraft.getInstance().options;
                SkillDataManager manager = playerPatch.getSkill(SkillSlots.WEAPON_INNATE).getDataManager();
                if(manager.getDataValue(InvincibleSkillDataKeys.UP) != options.keyUp.isDown()){
                    manager.setDataSync(InvincibleSkillDataKeys.UP, options.keyUp.isDown(), playerPatch.getOriginal());
                }
                if(manager.getDataValue(InvincibleSkillDataKeys.DOWN) != options.keyDown.isDown()){
                    manager.setDataSync(InvincibleSkillDataKeys.DOWN, options.keyDown.isDown(), playerPatch.getOriginal());
                }
                if(manager.getDataValue(InvincibleSkillDataKeys.LEFT) != options.keyLeft.isDown()){
                    manager.setDataSync(InvincibleSkillDataKeys.LEFT, options.keyLeft.isDown(), playerPatch.getOriginal());
                }
                if(manager.getDataValue(InvincibleSkillDataKeys.RIGHT) != options.keyRight.isDown()){
                    manager.setDataSync(InvincibleSkillDataKeys.RIGHT, options.keyRight.isDown(), playerPatch.getOriginal());
                }
            }
        }

        if (INPUT_QUEUE.size() > 2) {
            KeyMapping keyMapping = INPUT_QUEUE.poll();
            if (!INPUT_QUEUE.contains(keyMapping)) {
                KEY_STATE_CACHE.put(keyMapping, false);
            }
        }

    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (localPlayerPatch != null && Minecraft.getInstance().screen == null && !Minecraft.getInstance().isPaused()) {
            if (localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
                check(event.getButton(), event.getAction());
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && Minecraft.getInstance().screen == null && !Minecraft.getInstance().isPaused()) {
            if (event.getAction() == 1 && playerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
                check(event.getKey(), event.getAction());
            }
        }

    }

    public static void check(int key, int action) {
        for (KeyMapping keyMapping : KEY_STATE_CACHE.keySet()) {
            if (action == 1 && key == keyMapping.getKey().getValue()) {
                INPUT_QUEUE.add(keyMapping);
                KEY_STATE_CACHE.put(keyMapping, true);
                setDelay(SkillSlots.WEAPON_INNATE);
            }
        }
    }

    /**
     * 清理存下的按键，成功执行才清除
     */
    public static void clearKeyCache() {
        KEY_STATE_CACHE.forEach(((keyMapping, aBoolean) -> KEY_STATE_CACHE.put(keyMapping, false)));
    }

    public static void clearReservedKeys() {
        reserveCounter = -1;
        reservedSkillSlot = null;
    }

    /**
     * 延迟输入计时器，影响双键按下
     */
    public static void setDelayCounter(int delayCounter) {
        InputManager.delayCounter = delayCounter;
    }

    public static void setDelay(SkillSlot slot) {
        //不能被顶掉
        if (InputManager.delayCounter <= 0) {
            InputManager.delayCounter = Config.INPUT_DELAY_TICK.get();
            InputManager.currentSlot = slot;
        }
    }

    /**
     * 延迟输入的技能栏
     */
    public static void setCurrentSlot(SkillSlot currentSlot) {
        InputManager.currentSlot = currentSlot;
    }

    /**
     * 预存计时器
     */
    public static void setReserveCounter(int reserveCounter) {
        InputManager.reserveCounter = reserveCounter;
    }

    public static void setReserve(SkillSlot reserve) {
        InputManager.reserveCounter = Config.RESERVE_TICK.get();
        InputManager.reservedSkillSlot = reserve;
    }

    /**
     * 预存的技能栏
     */
    public static void setReservedSkillSlot(SkillSlot reservedSkillSlot) {
        InputManager.reservedSkillSlot = reservedSkillSlot;
    }

    /**
     * 发起执行请求，并预存键位，战斗模式下才可以使用
     */
    public static boolean tryRequestSkillExecute(SkillSlot slot, boolean shouldReserve) {
        LocalPlayerPatch executor = ClientEngine.getInstance().getPlayerPatch();
        if (executor != null && executor.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT) {
            if (sendExecuteRequest(executor, executor.getSkill(slot)).shouldReserverKey()) {
                if(shouldReserve){
                    setReserve(slot);
                }
                return false;
            } else {
                clearKeyCache();
                return true;
            }
        }
        return false;
    }

    public static SkillExecuteEvent sendExecuteRequest(LocalPlayerPatch executor, SkillContainer container) {
        SkillExecuteEvent event = new SkillExecuteEvent(executor, container);
        if (container.canExecute(executor, event)) {
            CPExecuteSkill packet = getExecutionPacket(container);
            if(packet != null) {
                EpicFightNetworkManager.sendToServer(packet);
            }
        }
        return event;
    }

    /**
     * @return 没有对应的触发就返回null
     */
    @Nullable
    public static CPExecuteSkill getExecutionPacket(SkillContainer container) {
        CPExecuteSkill packet = new CPExecuteSkill(container.getSlot());
        List<ComboType> typeList = new ArrayList<>(ComboType.ENUM_MANAGER.universalValues().stream().toList());
        typeList.sort(Comparator.comparingInt((comboType) -> -1 * comboType.getSubTypes().size()));//subType多的优先
        for(ComboType comboType : typeList){
            if(test(comboType)){
                packet.buffer().writeInt(comboType.universalOrdinal());
                return packet;
            }
        }
        return null;
    }

    public static boolean test(ComboType comboType){
        if(comboType.getSubTypes().isEmpty()){
            return KEY_STATE_CACHE.get(TYPE_KEY_MAP.get(comboType));
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
