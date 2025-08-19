package com.p1nero.invincible.api.combo;

import com.mojang.datafixers.util.Pair;
import com.p1nero.invincible.api.Side;
import com.p1nero.invincible.api.events.*;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class ComboNode {
    private int id;
    @NotNull
    protected ComboNode root;
    protected final Map<ComboType, ComboNode> children = new HashMap<>();
    @Nullable
    protected AnimationManager.AnimationAccessor<? extends StaticAnimation> animationAccessor;
    private int priority;
    protected float playSpeed, convertTime;
    private ValueModifier damageMultiplier = null;
    private float impactMultiplier = 1.0F;

    private float hurtDamageMultiplier;
    private float armorNegation;
    private StunType stunTypeModifier = null;
    private boolean canBeInterrupt = true;
    protected boolean notCharge;
    //自定义阶段
    protected int newPhase;
    protected int cooldown;
    protected List<Pair<Condition, Side>> conditions = new ArrayList<>();
    protected List<Supplier<Condition>> conditionProviders = new ArrayList<>();
    protected List<ComboNode> conditionAnimations = new ArrayList<>();
    protected final List<TimeStampedEvent> events = new ArrayList<>();
    protected final List<BiEvent> dodgeSuccessEvents = new ArrayList<>();
    protected final List<BiEvent> hitEvents = new ArrayList<>();
    protected final List<BiEvent> hurtEvents = new ArrayList<>();
    protected final List<BiEvent> onBeginEvents = new ArrayList<>();

    protected ComboNode() {
        root = this;
        ComboNodeManager.assignId(this);
    }

    public boolean isAssigned() {
        return id != 0;
    }

    public int getId() {
        return id;
    }

    public void assign(int id) {
        this.id = id;
    }

    public ComboNode setArmorNegation(float armorNegation) {
        this.armorNegation = armorNegation;
        return this;
    }

    public float getArmorNegation() {
        return armorNegation;
    }

    public float getHurtDamageMultiplier() {
        return hurtDamageMultiplier;
    }

    public ComboNode setHurtDamageMultiplier(float hurtDamageMultiplier) {
        this.hurtDamageMultiplier = hurtDamageMultiplier;
        return this;
    }

    public ValueModifier getDamageMultiplier() {
        return damageMultiplier;
    }

    public ComboNode setDamageMultiplier(ValueModifier damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
        return this;
    }

    public float getImpactMultiplier() {
        return impactMultiplier;
    }

    public ComboNode setImpactMultiplier(float impactMultiplier) {
        this.impactMultiplier = impactMultiplier;
        return this;
    }

    public StunType getStunTypeModifier() {
        return stunTypeModifier;
    }

    public ComboNode setStunTypeModifier(StunType stunTypeModifier) {
        this.stunTypeModifier = stunTypeModifier;
        return this;
    }

    public boolean isCanBeInterrupt() {
        return canBeInterrupt;
    }

    public ComboNode setCanBeInterrupt(boolean canBeInterrupt) {
        this.canBeInterrupt = canBeInterrupt;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public ComboNode setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public ComboNode setCooldown(int cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    public int getCooldown() {
        return cooldown;
    }

    public ComboNode setNewPhase(int newPhase) {
        this.newPhase = newPhase;
        return this;
    }

    public int getNewPhase() {
        return newPhase;
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

    public ComboNode addDodgeSuccessEvent(BiEvent event) {
        dodgeSuccessEvents.add(event);
        return this;
    }

    public ComboNode addHurtEvent(BiEvent event) {
        hurtEvents.add(event);
        return this;
    }

    public ComboNode addHitEvent(BiEvent event) {
        hitEvents.add(event);
        return this;
    }

    public ComboNode addBeginEvent(BiEvent event) {
        onBeginEvents.add(event);
        return this;
    }

    public List<TimeStampedEvent> getTimeEvents() {
        return events;
    }

    public List<BiEvent> getHitEvents() {
        return hitEvents;
    }

    public List<BiEvent> getHurtEvents() {
        return hurtEvents;
    }

    public List<BiEvent> getOnBeginEvents() {
        return onBeginEvents;
    }

    public List<BiEvent> getDodgeSuccessEvents() {
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

    public Collection<ComboNode> getChildren(){
        return this.children.values();
    }

    @Nullable
    public AnimationManager.AnimationAccessor<? extends StaticAnimation> getAnimationAccessor() {
        return animationAccessor == null ? null : animationAccessor;
    }

    public void setAnimationProvider(@Nullable AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        this.animationAccessor = animation;
    }

    @Nullable
    public AnimationManager.AnimationAccessor<? extends StaticAnimation> getAnimationProvider() {
        return animationAccessor;
    }

    @Nullable
    public ComboNode getNext(ComboType type) {
        return children.get(type);
    }

    public boolean hasNext() {
        return !children.isEmpty();
    }

    public static ComboNode create() {
        ComboNode root = new ComboNode();
        root.root = root;
        return root;
    }

    public static ComboNode createNode(@Nullable AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode node = new ComboNode();
        node.root = node;//先设自己，add的时候再换
        node.animationAccessor = animation;
        return node;
    }

    public ComboNode addLeaf(ComboType type, @Nullable AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(type, child);
        return child;
    }

    public ComboNode addChild(ComboType type, ComboNode child) {
        child.root = root;
        children.put(type, child);
        return this;
    }

    public boolean hasConditionAnimations() {
        return conditions.isEmpty();
    }

    /**
     * 默认加在服务端
     */
    public <T extends LivingEntityPatch<?>> ComboNode addCondition(@Nullable Condition<T> condition) {
        this.conditions.add(Pair.of(condition, Side.SERVER));
        return this;
    }
    public <T extends LivingEntityPatch<?>> ComboNode addClientCondition(@Nullable Condition<T> condition) {
        this.conditions.add(Pair.of(condition, Side.CLIENT));
        return this;
    }
    public <T extends LivingEntityPatch<?>> ComboNode addCondition(@Nullable Condition<T> condition, Side side) {
        this.conditions.add(Pair.of(condition, side));
        return this;
    }

    @NotNull
    public List<Condition> getConditions(Side... sides) {
        return conditions.stream()
                .filter(pair -> Arrays.stream(sides).anyMatch(side -> side == pair.getSecond()))
                .map(Pair::getFirst)
                .collect(Collectors.toList());
    }

    /**
     * 给数据包用，需延迟注册
     */
    public void addConditionProvider(Supplier<Condition> conditionProvider) {
        this.conditionProviders.add(conditionProvider);
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public void initConditions(){
        for(Supplier<Condition> conditionSupplier : conditionProviders) {
            Condition condition = conditionSupplier.get();
            if(condition != null) {
                this.addCondition(condition);
            }
        }
    }

    public ComboNode addConditionNode(ComboNode conditionAnimation) {
        this.conditionAnimations.add(conditionAnimation);
        return this;
    }

    public List<ComboNode> getConditionAnimations() {
        return conditionAnimations;
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

    public ComboNode keyWeaponInnate(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.WEAPON_INNATE, child);
        return this;
    }

    public ComboNode keyDodge(ComboNode child) {
        child.root = root;
        children.put(ComboTypes.DODGE, child);
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

    public ComboNode key1(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_1, child);
        return child;
    }

    public ComboNode key2(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_2, child);
        return child;
    }

    public ComboNode key3(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_3, child);
        return child;
    }

    public ComboNode key4(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_4, child);
        return child;
    }

    public ComboNode keyWeaponInnate(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.WEAPON_INNATE, child);
        return child;
    }

    public ComboNode keyDodge(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.DODGE, child);
        return child;
    }

    public ComboNode key1_2(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_1_2, child);
        return child;
    }

    public ComboNode key1_3(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_1_3, child);
        return child;
    }

    public ComboNode key1_4(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_1_4, child);
        return child;
    }

    public ComboNode key2_3(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_2_3, child);
        return child;
    }

    public ComboNode key2_4(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_2_4, child);
        return child;
    }

    public ComboNode key3_4(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
        ComboNode child = new ComboNode();
        child.animationAccessor = animation;
        child.root = root;
        children.put(ComboTypes.KEY_3_4, child);
        return child;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof ComboNode comboNode && comboNode.id == this.id);
    }

    public enum ComboTypes implements ComboType {
        KEY_1,
        KEY_2,
        KEY_3,
        KEY_4,
        KEY_1_2(KEY_1, KEY_2),
        KEY_1_3(KEY_1, KEY_3),
        KEY_1_4(KEY_1, KEY_4),
        KEY_2_3(KEY_2, KEY_3),
        KEY_2_4(KEY_2, KEY_4),
        KEY_3_4(KEY_3, KEY_4),
        DODGE,
        WEAPON_INNATE;
        final int id;

        final List<ComboType> subTypes;

        ComboTypes(ComboTypes... subTypes) {
            this.subTypes = List.of(subTypes);
            this.id = ComboType.ENUM_MANAGER.assign(this);
        }

        ComboTypes() {
            subTypes = new ArrayList<>();
            this.id = ComboType.ENUM_MANAGER.assign(this);
        }

        @Override
        public List<ComboType> getSubTypes() {
            return subTypes;
        }

        @Override
        public int universalOrdinal() {
            return id;
        }
    }

}
