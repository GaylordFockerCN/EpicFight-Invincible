package com.p1nero.invincible.capability;

import com.p1nero.invincible.skill.api.ComboNode;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class InvinciblePlayer {
    private ComboNode currentNode = null;
    private final List<TimeStampedEvent> timeStampedEvents = new ArrayList<>();
    public List<TimeStampedEvent> getEventList() {
        return timeStampedEvents;
    }

    public boolean addEvent(TimeStampedEvent event){
        return timeStampedEvents.add(event);
    }

    public void clearEvents(){
        timeStampedEvents.clear();
    }

    public ComboNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(ComboNode currentNode) {
        this.currentNode = currentNode;
    }

    public void saveNBTData(CompoundTag tag){
    }

    public void loadNBTData(CompoundTag tag){

    }

    public void copyFrom(InvinciblePlayer old){
        currentNode = old.currentNode;
    }

}
