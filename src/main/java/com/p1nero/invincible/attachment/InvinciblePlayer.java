package com.p1nero.invincible.attachment;

import com.google.common.collect.ImmutableList;
import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.api.skill.ComboNode;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.damagesource.StunType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvinciblePlayer implements INBTSerializable<CompoundTag> {
    private ComboNode currentNode = null;
    private ImmutableList<TimeStampedEvent> timeStampedEvents = null;
    private final Map<ItemStack, Integer> cooldownMap = new HashMap<>();
    @Nullable
    private ImmutableList<BiEvent> dodgeSuccessEvents = null;
    @Nullable
    private ImmutableList<BiEvent> hitSuccessEvents = null;
    @Nullable
    private ImmutableList<BiEvent> hurtEvents = null;
    private float playSpeedMultiplier;
    private ValueModifier damageMultiplier;
    private float armorNegation;
    private float impactMultiplier = 1.0F;
    private float hurtDamageMultiplier;
    private StunType stunTypeModifier = null;
    private boolean notCharge, canBeInterrupt = true;
    private int phase;
    public void setItemCooldown(ItemStack item, int cooldown){
        cooldownMap.put(item, cooldown);
    }

    public boolean isItemInCooldown(ItemStack item){
        if(!cooldownMap.containsKey(item)){
            return false;
        }
        return cooldownMap.get(item) >= 0;
    }

    public int getItemCooldown(ItemStack item){
        if(!cooldownMap.containsKey(item)){
            return 0;
        }
        return cooldownMap.get(item);
    }

    public float getArmorNegation() {
        return armorNegation;
    }

    public void setArmorNegation(float armorNegation) {
        this.armorNegation = armorNegation;
    }

    public float getHurtDamageMultiplier() {
        return hurtDamageMultiplier;
    }

    public void setHurtDamageMultiplier(float hurtDamageMultiplier) {
        this.hurtDamageMultiplier = hurtDamageMultiplier;
    }

    public ValueModifier getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(ValueModifier damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public float getImpactMultiplier() {
        return impactMultiplier;
    }

    public void setImpactMultiplier(float impactMultiplier) {
        this.impactMultiplier = impactMultiplier;
    }

    public StunType getStunTypeModifier() {
        return stunTypeModifier;
    }

    public void setStunTypeModifier(StunType stunTypeModifier) {
        this.stunTypeModifier = stunTypeModifier;
    }

    public boolean isCanBeInterrupt() {
        return canBeInterrupt;
    }

    public void setCanBeInterrupt(boolean canBeInterrupt) {
        this.canBeInterrupt = canBeInterrupt;
    }

    /**
     * 0 表示默认，防止被顶掉
     */
    public void setPhase(int phase) {
        if(phase != 0){
            this.phase = phase;
        }
    }

    public void resetPhase(){
        this.phase = 0;
    }

    public int getPhase() {
        return phase;
    }

    public void setNotCharge(boolean notCharge) {
        this.notCharge = notCharge;
    }

    public boolean isNotCharge() {
        return notCharge;
    }

    public void setPlaySpeedMultiplier(float playSpeedMultiplier) {
        this.playSpeedMultiplier = playSpeedMultiplier;
    }

    public float getPlaySpeedMultiplier() {
        return playSpeedMultiplier;
    }

    public List<TimeStampedEvent> getTimeEventList() {
        return timeStampedEvents;
    }

    public @Nullable ImmutableList<BiEvent> getDodgeSuccessEvents() {
        return dodgeSuccessEvents;
    }

    public @Nullable ImmutableList<BiEvent> getHurtEvents() {
        return hurtEvents;
    }

    public @Nullable ImmutableList<BiEvent> getHitSuccessEvents() {
        return hitSuccessEvents;
    }

    public void setDodgeSuccessEvents(@Nullable ImmutableList<BiEvent> dodgeSuccessEvents) {
        this.dodgeSuccessEvents = dodgeSuccessEvents;
    }

    public void setHitSuccessEvents(@Nullable ImmutableList<BiEvent> hitSuccessEvents) {
        this.hitSuccessEvents = hitSuccessEvents;
    }

    public void setHurtEvents(@Nullable ImmutableList<BiEvent> hurtEvents) {
        this.hurtEvents = hurtEvents;
    }

    public void setTimeStampedEvents(ImmutableList<TimeStampedEvent> event) {
        this.timeStampedEvents = event;
    }

    public void resetTimeEvents() {
        timeStampedEvents = null;
    }

    public ComboNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(ComboNode currentNode) {
        this.currentNode = currentNode;
    }

    public void clear(){
        playSpeedMultiplier = 0;
        damageMultiplier = null;
        impactMultiplier = 0;
        hurtDamageMultiplier = 0;
        stunTypeModifier = null;
        canBeInterrupt = true;
        notCharge = false;
        timeStampedEvents = null;
        dodgeSuccessEvents = null;
        hitSuccessEvents = null;
        hurtEvents = null;
    }

    public CompoundTag saveNBTData(CompoundTag tag) {
        tag.putBoolean("notCharge", notCharge);
        tag.putFloat("playSpeed", playSpeedMultiplier);
        return tag;
    }

    public void loadNBTData(CompoundTag tag) {
        notCharge = tag.getBoolean("notCharge");
        playSpeedMultiplier = tag.getFloat("playSpeed");
    }

    /**
     * 重生的时候仅需要复制连段数据
     */
    public void copyFrom(InvinciblePlayer old) {
        currentNode = old.currentNode;
    }

    public void tick() {
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag compoundTag) {
        notCharge = compoundTag.getBoolean("notCharge");
        playSpeedMultiplier = compoundTag.getFloat("playSpeed");
    }
}
