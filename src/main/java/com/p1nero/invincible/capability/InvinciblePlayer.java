package com.p1nero.invincible.capability;

import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.skill.api.ComboNode;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.damagesource.StunType;

import java.util.ArrayList;
import java.util.List;
public class InvinciblePlayer {
    private ComboNode currentNode = null;
    private final List<TimeStampedEvent> timeStampedEvents = new ArrayList<>();
    private final List<BiEvent> dodgeSuccessEvents = new ArrayList<>();
    private final List<BiEvent> hitSuccessEvents = new ArrayList<>();
    private final List<BiEvent> hurtEvents = new ArrayList<>();
    private float playSpeedMultiplier;
    private ValueModifier damageMultiplier;
    private float impactMultiplier;
    private float hurtDamageMultiplier;
    private StunType stunTypeModifier = StunType.NONE;
    private boolean notCharge, canBeInterrupt = true;
    private int phase;
    private int cooldown;

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

    public List<BiEvent> getDodgeSuccessEvents() {
        return dodgeSuccessEvents;
    }

    public List<BiEvent> getHurtEvents() {
        return hurtEvents;
    }

    public List<BiEvent> getHitSuccessEvents() {
        return hitSuccessEvents;
    }

    public boolean addTimeEvent(TimeStampedEvent event) {
        return timeStampedEvents.add(event);
    }

    public boolean addHurtEvent(BiEvent event) {
        return hurtEvents.add(event);
    }

    public boolean addDodgeSuccessEvent(BiEvent event) {
        return dodgeSuccessEvents.add(event);
    }

    public boolean addHitSuccessEvent(BiEvent event) {
        return hitSuccessEvents.add(event);
    }

    public void clearTimeEvents() {
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
        dodgeSuccessEvents.clear();
        hitSuccessEvents.clear();
        hurtEvents.clear();
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
