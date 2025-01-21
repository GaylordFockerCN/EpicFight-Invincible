package com.p1nero.invincible.skill.api;

import com.p1nero.invincible.api.events.BlockedEvent;
import com.p1nero.invincible.api.events.Event;
import com.p1nero.invincible.api.events.StunEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class ComboNode {
    @NotNull
    protected ComboNode root;
    protected final Map<ComboType, ComboNode> children = new HashMap<>();
    @Nullable
    protected AnimationProvider<?> animation;
    protected float playSpeed, convertTime;
    protected boolean notCharge;
    @Nullable
    protected Condition condition;
    protected final List<TimeStampedEvent> events = new ArrayList<>();
    protected final List<Event> dodgeSuccessEvents = new ArrayList<>();
    protected final List<Event> hitEvents = new ArrayList<>();
    protected final List<Event> hurtEvents = new ArrayList<>();
    protected final List<StunEvent> stunEvents = new ArrayList<>();
    protected final List<BlockedEvent> blockedEvents = new ArrayList<>();

    protected ComboNode() {
        root = this;
    }

    public ComboNode setNotCharge(boolean notCharge) {
        this.notCharge = notCharge;
        return this;
    }

    public boolean isNotCharge() {
        return notCharge;
    }

    public ComboNode setPlaySpeed(float playSpeed) {
        this.playSpeed = playSpeed;
        return this;
    }

    public float getPlaySpeed() {
        return playSpeed;
    }

    public ComboNode setConvertTime(float convertTime) {
        this.convertTime = convertTime;
        return this;
    }

    public float getConvertTime() {
        return convertTime;
    }

    public ComboNode addTimeEvent(TimeStampedEvent event) {
        events.add(event);
        return this;
    }

    public ComboNode addDodgeSuccessEvent(Event event) {
        dodgeSuccessEvents.add(event);
        return this;
    }

    public ComboNode addHurtEvent(Event event) {
        hurtEvents.add(event);
        return this;
    }

    public ComboNode addHitEvent(Event event) {
        hitEvents.add(event);
        return this;
    }

    public ComboNode addStunEvent(StunEvent event) {
        stunEvents.add(event);
        return this;
    }

    public ComboNode addBlockedEvent(BlockedEvent event) {
        blockedEvents.add(event);
        return this;
    }

    public List<TimeStampedEvent> getTimeEvents() {
        return events;
    }

    public List<BlockedEvent> getBlockedEvents() {
        return blockedEvents;
    }

    public List<Event> getHitEvents() {
        return hitEvents;
    }

    public List<Event> getHurtEvents() {
        return hurtEvents;
    }

    public List<StunEvent> getStunEvents() {
        return stunEvents;
    }

    public List<Event> getDodgeSuccessEvents() {
        return dodgeSuccessEvents;
    }

    public boolean isRoot() {
        return this.equals(root);
    }

    public boolean isEnd() {
        return children.isEmpty();
    }

    public ComboNode getRoot() {
        return root;
    }

    @Nullable
    public StaticAnimation getAnimation() {
        return animation == null ? null : animation.get();
    }

    @Nullable
    public AnimationProvider<?> getAnimationProvider() {
        return animation;
    }

    @Nullable
    public ComboNode getNext(ComboType type) {
        return children.get(type);
    }

    public boolean hasNext() {
        return !children.isEmpty();
    }

    public static ComboNode createRoot() {
        ComboNode root = new ComboNode();
        root.root = root;
        return root;
    }

    public static ComboNode createNode(AnimationProvider<?> animation) {
        ComboNode node = new ComboNode();
        node.root = node;//先设自己，add的时候再换
        node.animation = animation;
        return node;
    }

    public ComboNode addLeaf(ComboType type, AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(type, child);
        return child;
    }

    public ComboNode addChild(ComboType type, ComboNode child) {
        child.root = root;
        children.put(type, child);
        return this;
    }

    public boolean hasCondition() {
        return condition != null;
    }

    public <T extends LivingEntityPatch<?>> ComboNode setCondition(@Nullable Condition<T> condition) {
        this.condition = condition;
        return this;
    }

    public @Nullable <T extends LivingEntityPatch<?>> Condition<T> getCondition() {
        return condition;
    }

    public ComboNode key1(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_1, child);
        return this;
    }

    public ComboNode key2(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_2, child);
        return this;
    }

    public ComboNode key3(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_3, child);
        return this;
    }

    public ComboNode key4(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_4, child);
        return this;
    }

    public ComboNode key1_2(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_1_2, child);
        return this;
    }

    public ComboNode key1_3(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_1_3, child);
        return this;
    }

    public ComboNode key1_4(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_1_4, child);
        return this;
    }

    public ComboNode key2_3(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_2_3, child);
        return this;
    }

    public ComboNode key2_4(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_2_4, child);
        return this;
    }

    public ComboNode key3_4(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.KEY_3_4, child);
        return this;
    }

    public ComboNode key1(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_1, child);
        return child;
    }

    public ComboNode key2(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_2, child);
        return child;
    }

    public ComboNode key3(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_3, child);
        return child;
    }

    public ComboNode key4(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_4, child);
        return child;
    }

    public ComboNode key1_2(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_1_2, child);
        return child;
    }

    public ComboNode key1_3(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_1_3, child);
        return child;
    }

    public ComboNode key1_4(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_1_4, child);
        return child;
    }

    public ComboNode key2_3(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_2_3, child);
        return child;
    }

    public ComboNode key2_4(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_2_4, child);
        return child;
    }

    public ComboNode key3_4(AnimationProvider<?> animation) {
        ComboNode child = new ComboNode();
        child.animation = animation;
        child.root = root;
        children.put(ComboTypes.KEY_3_4, child);
        return child;
    }

    public enum ComboTypes implements ComboType {
        KEY_1, KEY_2, KEY_3, KEY_4, KEY_1_2, KEY_1_3, KEY_1_4, KEY_2_3, KEY_2_4, KEY_3_4;

        final boolean canPressTogether;
        final int id;

        ComboTypes(boolean canPressTogether) {
            this.canPressTogether = canPressTogether;
            this.id = ComboType.ENUM_MANAGER.assign(this);
        }

        ComboTypes() {
            this.canPressTogether = true;
            this.id = ComboType.ENUM_MANAGER.assign(this);
        }

        @Override
        public boolean canPressTogether() {
            return canPressTogether;
        }

        @Override
        public int universalOrdinal() {
            return id;
        }
    }

}
