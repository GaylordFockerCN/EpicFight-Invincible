package com.p1nero.invincible.capability;

import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.skill.api.ComboNode;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
public class InvinciblePlayer {
    private ComboNode currentNode = null;
    private final List<TimeStampedEvent> timeStampedEvents = new ArrayList<>();
    private final List<BiEvent> dodgeSuccessEvents = new ArrayList<>();
    private final List<BiEvent> hitSuccessEvents = new ArrayList<>();
    private final List<BiEvent> hurtEvents = new ArrayList<>();
    private float playSpeed;
    private boolean notCharge;
    private int phase;
    private int cooldown;

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setPhase(int phase) {
        this.phase = phase;
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

    public void setPlaySpeed(float playSpeed) {
        this.playSpeed = playSpeed;
    }

    public float getPlaySpeed() {
        return playSpeed;
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
        playSpeed = 0;
        notCharge = false;
        timeStampedEvents.clear();
        dodgeSuccessEvents.clear();
        hitSuccessEvents.clear();
        hurtEvents.clear();
    }

    public CompoundTag saveNBTData(CompoundTag tag) {
        tag.putBoolean("notCharge", notCharge);
        tag.putFloat("playSpeed", playSpeed);
        tag.putInt("cooldown", cooldown);
        return tag;
    }

    public void loadNBTData(CompoundTag tag) {
        notCharge = tag.getBoolean("notCharge");
        playSpeed = tag.getFloat("playSpeed");
        cooldown = tag.getInt("cooldown");
    }

    public void copyFrom(InvinciblePlayer old) {
        currentNode = old.currentNode;
    }

}
