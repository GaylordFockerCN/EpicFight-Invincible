package com.p1nero.invincible.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import com.p1nero.invincible.InvincibleConfig;
import com.p1nero.invincible.InvincibleFlags;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.Side;
import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.api.combo.ComboType;
import com.p1nero.invincible.attachment.InvincibleAttachments;
import com.p1nero.invincible.attachment.InvinciblePlayer;
import com.p1nero.invincible.compat.controlify.ControlifyCompat;
import com.p1nero.invincible.compat.controlify.ControlifyModAvailability;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.skill.ComboBasicAttack;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.neoevent.playerpatch.SkillCastEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = InvincibleMod.MOD_ID, value = Dist.CLIENT)
public class InputManager {

    private static int reserveCounter;
    private static long lastInputTime;
    private static long inputInterval;
    private static final BiMap<ComboType, @NotNull KeyMapping> TYPE_KEY_MAP = HashBiMap.create();
    private static BiMap<KeyMapping, ComboType> KEY_TYPE_MAP = HashBiMap.create();
    private static final Map<Integer, Integer> KEY_STATE_CACHE = new HashMap<>();
    private static final Queue<Integer> INPUT_QUEUE = new ArrayDeque<>();
    private static final List<CPSkillRequest> ON_PRESS_PACKETS = new ArrayList<>();
    private static ComboNode currentNode;

    public static ComboNode getCurrentNode() {
        return currentNode;
    }
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
        KEY_TYPE_MAP = TYPE_KEY_MAP.inverse();
    }

    /**
     * 获取某个按键的长按时间
     */
    public static int getPressedTickFor(ComboType comboType) {
        return testPressedTime(comboType);
    }

    @Nullable
    public static ComboBasicAttack getComboBasicSkill() {
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
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
    public static void onClientTick(ClientTickEvent.Post event) {
        maybeHandleControlifyRelease();
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();

        if (localPlayerPatch != null && shouldHandleInput()) {
            //缓存的按键的处理
            if (reserveCounter > 0) {
                --reserveCounter;
                if (tryRequestSkillExecute(false) || reserveCounter == 0) {
                    clearReservedKeys();
                    clearKeyCache();
                }
            } else {
                handlePressing();
            }

            //判断asdw是否按下，用于Condition判断。
            if (localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack) {
                Options options = Minecraft.getInstance().options;
                SkillDataManager manager = getDataManager(localPlayerPatch);
                checkDirectionKeyDown(manager, InvincibleSkillDataKeys.UP, options.keyUp);
                checkDirectionKeyDown(manager, InvincibleSkillDataKeys.DOWN, options.keyDown);
                checkDirectionKeyDown(manager, InvincibleSkillDataKeys.LEFT, options.keyLeft);
                checkDirectionKeyDown(manager, InvincibleSkillDataKeys.RIGHT, options.keyRight);

                //判断是否有任意按键按下（暂时无用）
                AtomicBoolean flag = new AtomicBoolean(false);
                KEY_STATE_CACHE.forEach((key, time) -> {
                    if(time != 0) {
                        flag.set(true);
                    }
                });
                if(flag.get() != manager.getDataValue(InvincibleSkillDataKeys.ANY_KEY_DOWN)) {
                    manager.setDataSync(InvincibleSkillDataKeys.ANY_KEY_DOWN, flag.get());
                }
            }

            //缓存的onPress包的处理
            if(!localPlayerPatch.getEntityState().inaction()) {
                ON_PRESS_PACKETS.forEach(EpicFightNetworkManager::sendToServer);
                ON_PRESS_PACKETS.clear();
            }
        }

        while (INPUT_QUEUE.size() > 2) {
            Integer keyId = INPUT_QUEUE.poll();
            if (!INPUT_QUEUE.contains(keyId)) {
                KEY_STATE_CACHE.put(keyId, 0);
            }
        }
    }

    public static SkillDataManager getDataManager(@NotNull LocalPlayerPatch localPlayerPatch) {
        return localPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getDataManager();
    }

    private static void checkDirectionKeyDown(SkillDataManager manager, DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<Boolean>> skillDataKey, KeyMapping key) {
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (manager.getDataValue(skillDataKey) != key.isDown() && localPlayerPatch != null) {
            manager.setDataSync(skillDataKey, key.isDown());
        }
    }


    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        onVanillaMouseOrKeyInput(event.getAction(), event.getButton());
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        onVanillaMouseOrKeyInput(event.getAction(), event.getKey());
    }

    private static void onVanillaMouseOrKeyInput(int action, int key) {
        if(!shouldHandleInput()) {
            return;
        }
        if (action == InputConstants.RELEASE && INPUT_QUEUE.contains(key)) {
            handleRelease();
        }
        if(action == InputConstants.PRESS) {
            handlePress(key);
        }
    }

    // TODO: Ideally, we should call handleRelease() from one place and provide
    //  standard handling for both controllers and mouse/keyboard without depending on "InputEvents"s
    //  or Controlify APIs directly.
    //  However, it's tricky since "KeyMapping#isDown" returns false whenever there are multiple keybinds
    //  that are bound to the same physical mouse button.
    //  As a workaround, we handle mouse/keyboard via "InputEvents"s, and Controlify via this method.
    //  The keybind presses handling is shared for all inputs using handleKeyBinds()
    //  This HACK can be eliminated in MC versions newer than 1.21.10
    //  See related Epic Fight issue
    //  (although that's a different issue, it's also because of this Minecraft bug):
    //  https://github.com/Epic-Fight/epicfight/issues/2174
    //  https://github.com/GaylordFockerCN/EpicFight-Invincible/issues/11

    private static void maybeHandleControlifyRelease() {
        if (!ControlifyModAvailability.isModInstalled()) {
            return;
        }
        final Optional<ControllerEntity> maybeController = ControlifyApi.get().getCurrentController();
        if (maybeController.isEmpty()) {
            return;
        }
        final ControllerEntity controller = maybeController.get();
        for (KeyMapping keyMapping : TYPE_KEY_MAP.values()) {
            final InputBindingSupplier inputBindingSupplier = ControlifyCompat.getInputBindingFromKeyMapping(keyMapping);
            if (inputBindingSupplier == null) {
                continue;
            }
            if (inputBindingSupplier.on(controller).justReleased()) {
                handleRelease();
            }
        }
    }

    private static void handleRelease() {
        if (shouldHandleInput()) {
            tryRequestSkillExecute(true);
        }
    }

    private static boolean shouldHandleInput() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.isPaused()) {
            return false;
        }
        final LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch == null) {
            return false;
        }
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        ItemStack mainHandItem = playerPatch.getOriginal().getMainHandItem();
        EpicFightCapabilities.getItemCapability(mainHandItem).ifPresent(capabilityItem -> {
            atomicBoolean.set(capabilityItem.getInnateSkill(playerPatch, mainHandItem) instanceof ComboBasicAttack);
        });
        if(!atomicBoolean.get()) {
            clearKeyCache();
            clearReservedKeys();
        }
        return atomicBoolean.get();
    }

    /**
     * 按下时记录
     * 松手时发包
     */
    private static void handlePress(int inputKey) {
        for (KeyMapping keyMapping : TYPE_KEY_MAP.values()) {
            int keyId = keyMapping.getKey().getValue();
            if (keyMapping.getKey().getValue() == inputKey) {
                executeOnPress(keyMapping);
                if (!INPUT_QUEUE.contains(keyId)) {
                    INPUT_QUEUE.add(keyId);
                }
                if(KEY_STATE_CACHE.getOrDefault(keyId, 0) <= 0) {
                    KEY_STATE_CACHE.put(keyId, 1);
                }
                clearReservedKeys();
            }
        }
    }

    /**
     * 发包给服务端执行onPress
     */
    private static void executeOnPress(KeyMapping keyMapping) {
        ComboType type = KEY_TYPE_MAP.get(keyMapping);
        CPSkillRequest packet = new CPSkillRequest(SkillSlots.WEAPON_INNATE, new CompoundTag());
        if(type == null || packet.arguments() == null) {
            return;
        }
        packet.arguments().putInt(InvincibleFlags.TYPE_ID, type.universalOrdinal());
        packet.arguments().putBoolean(InvincibleFlags.ON_PRESS, true);
        ON_PRESS_PACKETS.add(packet);
    }

    private static void handlePressing() {
        //没缓存时长按才算数
        AtomicBoolean shouldExecute = new AtomicBoolean(false);
        int maxPressTick = InvincibleConfig.MAX_PRESS_TICK.get();
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
            tryRequestSkillExecute(true);
        }
    }

    /**
     * 清理存下的按键，成功执行才清除
     */
    public static void clearKeyCache() {
        KEY_STATE_CACHE.forEach(((keyId, aInt) -> KEY_STATE_CACHE.put(keyId, 0)));
        INPUT_QUEUE.clear();
    }

    public static void clearReservedKeys() {
        reserveCounter = -1;
    }

    /**
     * 预存计时器
     */
    public static void setReserveCounter(int reserveCounter) {
        InputManager.reserveCounter = reserveCounter;
    }

    public static void setReserve(SkillSlot reserve) {
        InputManager.reserveCounter = InvincibleConfig.RESERVE_TICK.get();
        ComboBasicAttack comboBasicAttack = getComboBasicSkill();
        if (comboBasicAttack != null) {
            InputManager.reserveCounter = comboBasicAttack.getMaxReserveTime();
        }
    }

    /**
     * 发起执行请求，并预存键位，战斗模式下才可以使用
     */
    public static boolean tryRequestSkillExecute(boolean shouldReserve) {
        SkillSlot slot = SkillSlots.WEAPON_INNATE;
        LocalPlayerPatch executor = ClientEngine.getInstance().getPlayerPatch();
        if (executor != null && executor.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT) {
            if (sendExecuteRequest(executor, executor.getSkill(slot)).shouldReserveKey()) {
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

    public static SkillCastEvent sendExecuteRequest(LocalPlayerPatch executor, SkillContainer container) {
        SkillCastEvent event = new SkillCastEvent(executor, container, new CompoundTag());
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(executor.getOriginal());
        currentNode = invinciblePlayer.getCurrentNode();
        if (container.canUse(executor, event)) {
            for(CPSkillRequest packet : getAvailablePackets(container)){
                EpicFightNetworkManager.sendToServer(packet);
            }
        }
        return event;
    }

    /**
     * @return 返回所有可能触发的
     */
    public static List<CPSkillRequest> getAvailablePackets(SkillContainer container) {
        List<CPSkillRequest> list = new ArrayList<>();
        List<ComboType> typeList = new ArrayList<>(ComboType.ENUM_MANAGER.universalValues().stream().toList());
        typeList.sort(Comparator.comparingInt((comboType) -> -1 * comboType.getSubTypes().size()));//subType多的优先
        for (ComboType comboType : typeList) {
            int pressedTime = testPressedTime(comboType);
            if (pressedTime > 0 && testClientConditions(comboType)) {
                inputInterval = System.currentTimeMillis() - lastInputTime;
                list.add(getExecutePacket(container.getSlot(), comboType, pressedTime, inputInterval));
                if(!comboType.getSubTypes().isEmpty()) {
                    break;
                }
            }
        }
        //成功发了就更新上次按下的时间
        if(!list.isEmpty()) {
            lastInputTime = System.currentTimeMillis();
        }
        return list;
    }

    public static CPSkillRequest getExecutePacket(SkillSlot slot, ComboType comboType, int pressedTime, long inputInterval) {
        CPSkillRequest packet = new CPSkillRequest(slot, new CompoundTag());
        packet.arguments().putInt(InvincibleFlags.TYPE_ID, comboType.universalOrdinal());
        packet.arguments().putInt(InvincibleFlags.PRESSED_TIME, pressedTime);
        packet.arguments().putLong(InvincibleFlags.PRESSED_INTERVAL, inputInterval);
        return packet;
    }

    /**
     * 返回长按最大值，id相同的键都视为触发
     */
    public static int testPressedTime(ComboType comboType) {
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
                int currentPressedTime = testPressedTime(subType);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean testClientConditions(ComboType comboType) {
        if(currentNode == null) {
            return false;
        }
        ComboNode next = currentNode.getNext(comboType);
        if(next == null) {
            return false;
        }
        for(Condition condition : next.getConditions(Side.CLIENT, Side.LOCAL_CLIENT, Side.BOTH)) {
            LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
            if(!condition.predicate(localPlayerPatch)){
                return false;
            }
        }
        return true;
    }

}