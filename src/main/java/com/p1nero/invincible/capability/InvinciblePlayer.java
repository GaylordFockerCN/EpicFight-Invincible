package com.p1nero.invincible.capability;

import com.google.common.collect.ImmutableList;
import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.skill.api.ComboNode;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.damagesource.StunType;

import java.util.ArrayList;
import java.util.List;

public class InvinciblePlayer {
    private ComboNode currentNode = null;
    private final ArrayList<TimeStampedEvent> timeStampedEvents = new ArrayList<>();
    @Nullable
    private ImmutableList<BiEvent> dodgeSuccessEvents = null;
    @Nullable
    private ImmutableList<BiEvent> hitSuccessEvents = null;
    @Nullable
    private ImmutableList<BiEvent> hurtEvents = null;
    private float playSpeedMultiplier;
    private ValueModifier damageMultiplier;
    private float armorNegation;
    private float impactMultiplier;
    private float hurtDamageMultiplier;
    private StunType stunTypeModifier = StunType.NONE;
    private boolean notCharge, canBeInterrupt = true;
    private int phase;
    private int cooldown;

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
    public void setCooldown(int cooldown) {
        if(cooldown != 0){
            this.cooldown = cooldown;
        }
    }

    public int getCooldown() {
        return cooldown;
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

    public void addTimeEvent(TimeStampedEvent event) {
        this.timeStampedEvents.add(event);
    }

    public void resetTimeEvents() {
        timeStampedEvents.clear();
    }

    public ComboNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(ComboNode currentNode) {
        this.currentNode = currentNode;
    }

    public void tick() {
        if(cooldown > 0){
            cooldown--;
        }
    }

    public void clear(){
        playSpeedMultiplier = 0;
        damageMultiplier = null;
        impactMultiplier = 0;
        hurtDamageMultiplier = 0;
        stunTypeModifier = null;
        canBeInterrupt = true;
        notCharge = false;
        dodgeSuccessEvents = null;
        hitSuccessEvents = null;
        hurtEvents = null;
    }

    public CompoundTag saveNBTData(CompoundTag tag) {
        tag.putBoolean("notCharge", notCharge);
        tag.putFloat("playSpeed", playSpeedMultiplier);
        tag.putInt("cooldown", cooldown);
        return tag;
    }

    public void loadNBTData(CompoundTag tag) {
        notCharge = tag.getBoolean("notCharge");
        playSpeedMultiplier = tag.getFloat("playSpeed");
        cooldown = tag.getInt("cooldown");
    }

    /**
     * 重生的时候仅需要复制连段数据
     */
    public void copyFrom(InvinciblePlayer old) {
        currentNode = old.currentNode;
    }

}
