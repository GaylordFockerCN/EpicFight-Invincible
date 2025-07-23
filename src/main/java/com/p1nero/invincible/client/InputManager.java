package com.p1nero.invincible.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.p1nero.invincible.Config;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.api.combo.ComboType;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import yesman.epicfight.api.neoforgeevent.playerpatch.SkillExecuteEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPExecuteSkill;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = InvincibleMod.MOD_ID, value = Dist.CLIENT)
public class InputManager {

    private static int reserveCounter;
    private static SkillSlot reservedSkillSlot;
    private static final Map<ComboType, KeyMapping> TYPE_KEY_MAP = new HashMap<>();
    private static final Map<Integer, Integer> KEY_STATE_CACHE = new HashMap<>();
    private static final Queue<Integer> INPUT_QUEUE = new ArrayDeque<>();
    private static LocalPlayerPatch localPlayerPatch;

    /**
     * 绑定模组自带的的按键
     */
    public static void init() {
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
    public static void register(ComboType type, KeyMapping keyMapping) {
        TYPE_KEY_MAP.put(type, keyMapping);
        KEY_STATE_CACHE.put(keyMapping.getKey().getValue(), 0);
    }

    /**
     * 获取某个按键的长按时间
     */
    public static int getPressedTickFor(ComboType comboType) {
        return test(comboType);
    }

    @Nullable
    public static ComboBasicAttack getComboBasicSkill() {
        if (localPlayerPatch == null) {
            return null;
        } else if (localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack comboBasicAttack) {
            return comboBasicAttack;
        }
        return null;
    }

    /**
     * 用史诗战斗的那套可能会被顶掉所以自己写了预存
     * 同时给了按键输入一点小延迟，方便读取双键
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if (localPlayerPatch == null) {
            localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        }
        if (localPlayerPatch != null) {
            //缓存的按键的处理
            if (reserveCounter > 0) {
                --reserveCounter;
                if (tryRequestSkillExecute(reservedSkillSlot, false)) {
                    clearReservedKeys();
                    clearKeyCache();
                }
                if (reserveCounter == 0) {
                    clearReservedKeys();
                    clearKeyCache();
                }
            } else {
                handlePressing();
            }

            //判断asdw是否按下，用于Condition判断。
            if (localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
                Options options = Minecraft.getInstance().options;
                SkillDataManager manager = localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getDataManager();
                checkDirectionKeyDown(manager, InvincibleSkillDataKeys.UP, options.keyUp);
                checkDirectionKeyDown(manager, InvincibleSkillDataKeys.DOWN, options.keyDown);
                checkDirectionKeyDown(manager, InvincibleSkillDataKeys.LEFT, options.keyLeft);
                checkDirectionKeyDown(manager, InvincibleSkillDataKeys.RIGHT, options.keyRight);
            }
        }

        if (INPUT_QUEUE.size() > 2) {
            Integer keyId = INPUT_QUEUE.poll();
            if (!INPUT_QUEUE.contains(keyId)) {
                KEY_STATE_CACHE.put(keyId, 0);
            }
        }

    }

    private static void checkDirectionKeyDown(SkillDataManager manager, DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<Boolean>> skillDataKey, KeyMapping key) {
        if (manager.getDataValue(skillDataKey) != key.isDown() && localPlayerPatch != null) {
            manager.setDataSync(skillDataKey, key.isDown(), localPlayerPatch.getOriginal());
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        handleInput(event.getButton(), event.getAction());
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        handleInput(event.getKey(), event.getAction());
    }

    /**
     * 按下时记录
     * 松手时发包
     */
    private static void handleInput(int key, int action) {
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && Minecraft.getInstance().screen == null && !Minecraft.getInstance().isPaused()
                && playerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
            if (action == InputConstants.PRESS) {
                for (KeyMapping keyMapping : TYPE_KEY_MAP.values()) {
                    int keyId = keyMapping.getKey().getValue();
                    if (key == keyId) {
                        if (!INPUT_QUEUE.contains(keyId)) {
                            INPUT_QUEUE.add(keyId);
                        }
                        KEY_STATE_CACHE.put(keyId, KEY_STATE_CACHE.getOrDefault(keyId, 0) + 1);
                        clearReservedKeys();
                    }
                }
            }
            if (action == InputConstants.RELEASE) {
                tryRequestSkillExecute(SkillSlots.WEAPON_INNATE, true);
            }
        }
    }

    private static void handlePressing() {
        //没缓存时长按才算数
        AtomicBoolean shouldExecute = new AtomicBoolean(false);
        int maxPressTick = Config.MAX_PRESS_TICK.get();
        ComboBasicAttack comboBasicAttack = getComboBasicSkill();
        if (comboBasicAttack != null) {
            maxPressTick = comboBasicAttack.getMaxPressTime();
        }
        int finalMaxPressTick = maxPressTick;
        KEY_STATE_CACHE.forEach((keyId, integer) -> {
            if (integer > 0) {
                KEY_STATE_CACHE.put(keyId, integer + 1);
                if (integer > finalMaxPressTick) {
                    shouldExecute.set(true);
                }
            }
        });
        if (shouldExecute.get()) {
            tryRequestSkillExecute(SkillSlots.WEAPON_INNATE, true);
        }
    }

    /**
     * 清理存下的按键，成功执行才清除
     */
    public static void clearKeyCache() {
        KEY_STATE_CACHE.forEach(((keyId, aInt) -> KEY_STATE_CACHE.put(keyId, 0)));
    }

    public static void clearReservedKeys() {
        reserveCounter = -1;
        reservedSkillSlot = null;
    }

    /**
     * 预存计时器
     */
    public static void setReserveCounter(int reserveCounter) {
        InputManager.reserveCounter = reserveCounter;
    }

    public static void setReserve(SkillSlot reserve) {
        InputManager.reserveCounter = Config.RESERVE_TICK.get();
        ComboBasicAttack comboBasicAttack = getComboBasicSkill();
        if (comboBasicAttack != null) {
            InputManager.reserveCounter = comboBasicAttack.getMaxReserveTime();
        }
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
                if (shouldReserve) {
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
            for(CPExecuteSkill packet : getAvailablePackets(container)){
                EpicFightNetworkManager.sendToServer(packet);
            }
        }
        return event;
    }

    /**
     * @return 返回所有可能触发的
     */
    public static List<CPExecuteSkill> getAvailablePackets(SkillContainer container) {
        List<CPExecuteSkill> list = new ArrayList<>();
        List<ComboType> typeList = new ArrayList<>(ComboType.ENUM_MANAGER.universalValues().stream().toList());
        typeList.sort(Comparator.comparingInt((comboType) -> -1 * comboType.getSubTypes().size()));//subType多的优先
        for (ComboType comboType : typeList) {
            int pressedTime = test(comboType);
            if (pressedTime > 0) {
                list.add(getExecutePacket(container.getSlot(), comboType, pressedTime));
                if(!comboType.getSubTypes().isEmpty()) {
                    return list;
                }
            }
        }
        return list;
    }

    public static CPExecuteSkill getExecutePacket(SkillSlot slot, ComboType comboType, int pressedTime) {
        CPExecuteSkill packet = new CPExecuteSkill(slot);
        packet.buffer().writeInt(comboType.universalOrdinal());
        packet.buffer().writeInt(pressedTime);
        return packet;
    }

    /**
     * 返回长按最大值，id相同的键都视为触发
     */
    public static int test(ComboType comboType) {
        if (comboType.getSubTypes().isEmpty()) {
            KeyMapping keyMapping = TYPE_KEY_MAP.get(comboType);
            if(keyMapping == null || !KEY_STATE_CACHE.containsKey(keyMapping.getKey().getValue())) {
                return 0;
            }
            int pressedTime = KEY_STATE_CACHE.getOrDefault(keyMapping.getKey().getValue(), 0);
            return Math.max(pressedTime, 0);
        } else {
            int maxPressedTime = 0;
            for (ComboType subType : comboType.getSubTypes()) {
                int currentPressedTime = test(subType);
                if (currentPressedTime == 0) {
                    return 0;
                }
                if (currentPressedTime > maxPressedTime) {
                    maxPressedTime = currentPressedTime;
                }
            }
            return maxPressedTime;
        }
    }

}
