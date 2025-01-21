package com.p1nero.invincible.capability;

import com.p1nero.invincible.Config;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.skill.api.ComboNode;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class InvinciblePlayer {
    private ComboNode currentNode = null;
    private final List<TimeStampedEvent> timeStampedEvents = new ArrayList<>();
    private final List<TimeStampedEvent> dodgeSuccessEvents = new ArrayList<>();
    private final List<TimeStampedEvent> hitSuccessEvents = new ArrayList<>();
    private final List<TimeStampedEvent> hurtEvents = new ArrayList<>();
    private final List<TimeStampedEvent> blockEvents = new ArrayList<>();
    private int dodgeSuccessTimer, hitSuccessTimer, parrySuccessTimer;
    private float playSpeed;
    private boolean notCharge;

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

    public void onDodgeSuccess() {
        dodgeSuccessTimer = Config.EFFECT_TICK.get();
    }

    public void onHitSuccess() {
        hitSuccessTimer = Config.EFFECT_TICK.get();
    }

    public void onParrySuccess() {
        parrySuccessTimer = Config.EFFECT_TICK.get();
    }

    public boolean dodgeSuccess() {
        return dodgeSuccessTimer > 0;
    }

    public boolean hitSuccess() {
        return hitSuccessTimer > 0;
    }

    public boolean parrySuccess() {
        return parrySuccessTimer > 0;
    }

    public List<TimeStampedEvent> getEventList() {
        return timeStampedEvents;
    }

    public List<TimeStampedEvent> getDodgeSuccessEvents() {
        return dodgeSuccessEvents;
    }

    public List<TimeStampedEvent> getBlockEvents() {
        return blockEvents;
    }

    public List<TimeStampedEvent> getHurtEvents() {
        return hurtEvents;
    }

    public int getHitSuccessTimer() {
        return hitSuccessTimer;
    }

    public int getParrySuccessTimer() {
        return parrySuccessTimer;
    }

    public boolean addTimeEvent(TimeStampedEvent event) {
        return timeStampedEvents.add(event);
    }

    public boolean addHurtEvent(TimeStampedEvent event) {
        return hurtEvents.add(event);
    }

    public boolean addDodgeSuccessEvent(TimeStampedEvent event) {
        return dodgeSuccessEvents.add(event);
    }

    public boolean addHitSuccessEvent(TimeStampedEvent event) {
        return hitSuccessEvents.add(event);
    }

    public boolean addBlockEvent(TimeStampedEvent event) {
        return blockEvents.add(event);
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
        if (dodgeSuccessTimer > 0) {
            dodgeSuccessTimer--;
        }
        if (hitSuccessTimer > 0) {
            hitSuccessTimer--;
        }
        if (parrySuccessTimer > 0) {
            parrySuccessTimer--;
        }
    }

    public void clear(){
        playSpeed = 0;
        notCharge = false;
        timeStampedEvents.clear();
        dodgeSuccessEvents.clear();
        hitSuccessEvents.clear();
        hurtEvents.clear();
        blockEvents.clear();
    }

    public void saveNBTData(CompoundTag tag) {
    }

    public void loadNBTData(CompoundTag tag) {

    }

    public void copyFrom(InvinciblePlayer old) {
        currentNode = old.currentNode;
    }

}
