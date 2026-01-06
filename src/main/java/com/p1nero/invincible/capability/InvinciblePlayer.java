package com.p1nero.invincible.capability;

import com.google.common.collect.ImmutableList;
import com.p1nero.invincible.api.events.BaseEvent;
import com.p1nero.invincible.api.events.TimePeriodEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.api.skill.ComboNodeManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.damagesource.StunType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvinciblePlayer {
    //当前连段位置
    private ComboNode currentLogicNode = null;
    //当前数据
    private ComboNode currentDataNode = ComboNode.EMPTY;
    private final Map<ItemStack, Integer> cooldownMap = new HashMap<>();
    private int phase;

    public void setItemCooldown(ItemStack item, int cooldown) {
        cooldownMap.put(item, cooldown);
    }

    public boolean isItemInCooldown(ItemStack item) {
        if (!cooldownMap.containsKey(item)) {
            return false;
        }
        return cooldownMap.get(item) >= 0;
    }

    public int getItemCooldown(ItemStack item) {
        if (!cooldownMap.containsKey(item)) {
            return 0;
        }
        return cooldownMap.get(item);
    }

    public float getArmorNegation() {
        return currentDataNode.getArmorNegation();
    }

    public float getHurtDamageMultiplier() {
        return currentDataNode.getHurtDamageMultiplier();
    }

    public ValueModifier getDamageMultiplier() {
        return currentDataNode.getDamageMultiplier();
    }

    public float getImpactMultiplier() {
        return currentDataNode.getImpactMultiplier();
    }

    public StunType getStunTypeModifier() {
        return currentDataNode.getStunTypeModifier();
    }

    public boolean canBeInterrupt() {
        return currentDataNode.isCanBeInterrupt();
    }

    public boolean isNotCharge() {
        return currentDataNode.isNotCharge();
    }

    public float getPlaySpeedMultiplier() {
        return currentDataNode.getPlaySpeed();
    }

    @Nullable
    public List<TimeStampedEvent> getTimeEventList() {
        return currentDataNode.getTimeEvents();
    }

    @Nullable
    public List<BaseEvent> getDodgeSuccessEvents() {
        return currentDataNode.getDodgeSuccessEvents();
    }

    @Nullable
    public List<BaseEvent> getHurtEvents() {
        return currentDataNode.getHurtEvents();
    }

    @Nullable
    public List<BaseEvent> getHitSuccessEvents() {
        return currentDataNode.getHitEvents();
    }

    @Nullable
    public List<TimePeriodEvent> getTimePeriodEvents() {
        return currentDataNode.getTimePeriodEvents();
    }

    /**
     * 0 表示默认，防止被顶掉
     */
    public void setPhase(int phase) {
        if (phase != 0) {
            this.phase = phase;
        }
    }

    public void resetPhase() {
        this.phase = 0;
    }

    public int getPhase() {
        return phase;
    }

    @Deprecated
    public ComboNode getCurrentNode() {
        return currentLogicNode;
    }

    @Deprecated
    public void setCurrentNode(ComboNode currentLogicNode) {
        this.currentLogicNode = currentLogicNode;
    }

    public ComboNode getCurrentLogicNode() {
        return currentLogicNode;
    }

    public void setCurrentLogicNode(ComboNode currentLogicNode) {
        this.currentLogicNode = currentLogicNode;
    }

    public void setCurrentDataNode(ComboNode currentDataNode) {
        this.currentDataNode = currentDataNode;
    }

    public ComboNode getCurrentDataNode() {
        return currentDataNode;
    }

    public void clear() {
        currentDataNode = ComboNode.EMPTY;
    }

    public CompoundTag saveNBTData(CompoundTag tag) {
        if (currentLogicNode != null) {
            tag.putInt("currentLogicNodeId", currentLogicNode.getId());
        }
        if (currentDataNode != null) {
            tag.putInt("currentDataNodeId", currentDataNode.getId());
        }
        return tag;
    }

    public void loadNBTData(CompoundTag tag) {
        int currentLogicNodeId = tag.getInt("currentLogicNodeId");
        if (currentLogicNodeId != 0) {
            currentLogicNode = ComboNodeManager.get(currentLogicNodeId);
        }
        int currentDataNodeId = tag.getInt("currentDataNodeId");
        if (currentDataNodeId != 0) {
            currentDataNode = ComboNodeManager.get(currentDataNodeId);
        }
    }

    /**
     * 重生的时候仅需要复制连段数据
     */
    public void copyFrom(InvinciblePlayer old) {
        currentLogicNode = old.currentLogicNode;
    }

}
