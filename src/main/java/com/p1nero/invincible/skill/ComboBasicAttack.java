package com.p1nero.invincible.skill;

import com.google.common.collect.ImmutableList;
import com.p1nero.invincible.Config;
import com.p1nero.invincible.api.animation.StaticAnimationProvider;
import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.capability.InvinciblePlayer;
import com.p1nero.invincible.client.events.InputManager;
import com.p1nero.invincible.conditions.Condition;
import com.p1nero.invincible.item.InvincibleItems;
import com.p1nero.invincible.skill.api.ComboNode;
import com.p1nero.invincible.skill.api.ComboType;
import net.minecraft.client.player.Input;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ComboBasicAttack extends Skill {

    protected static final UUID EVENT_UUID = UUID.fromString("d1d114cc-f11f-11ed-a05b-0242ac114514");
    public static SkillDataManager.SkillDataKey<Integer> DODGE_SUCCESS_TIMER = SkillDataManager.SkillDataKey.createDataKey(SkillDataManager.ValueType.INTEGER);
    public static SkillDataManager.SkillDataKey<Integer> COOLDOWN_TIMER = SkillDataManager.SkillDataKey.createDataKey(SkillDataManager.ValueType.INTEGER);
    public static SkillDataManager.SkillDataKey<Integer> PARRY_TIMER = SkillDataManager.SkillDataKey.createDataKey(SkillDataManager.ValueType.INTEGER);
    public static SkillDataManager.SkillDataKey<Boolean> UP = SkillDataManager.SkillDataKey.createDataKey(SkillDataManager.ValueType.BOOLEAN);
    public static SkillDataManager.SkillDataKey<Boolean> DOWN = SkillDataManager.SkillDataKey.createDataKey(SkillDataManager.ValueType.BOOLEAN);
    public static SkillDataManager.SkillDataKey<Boolean> LEFT = SkillDataManager.SkillDataKey.createDataKey(SkillDataManager.ValueType.BOOLEAN);
    public static SkillDataManager.SkillDataKey<Boolean> RIGHT = SkillDataManager.SkillDataKey.createDataKey(SkillDataManager.ValueType.BOOLEAN);

    @OnlyIn(Dist.CLIENT)
    protected boolean isWalking;

    protected boolean shouldDrawGui;

    @Nullable
    protected StaticAnimationProvider walkBegin, walkEnd;

    protected ComboNode root;

    public static Builder createComboBasicAttack() {
        return new Builder().setCategory(SkillCategories.WEAPON_INNATE).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.NONE);
    }

    public ComboBasicAttack(Builder builder) {
        super(builder);
        shouldDrawGui = builder.shouldDrawGui;
        root = builder.root;
        walkBegin = builder.walkBegin;
        walkEnd = builder.walkEnd;
    }

    @Override
    public boolean canExecute(PlayerPatch<?> executer) {
        if (executer.isLogicalClient()) {
            return super.canExecute(executer);
        } else {
            ItemStack itemstack = executer.getOriginal().getMainHandItem();
            return super.canExecute(executer) && EpicFightCapabilities.getItemStackCapability(itemstack).getInnateSkill(executer, itemstack) == this && executer.getOriginal().getVehicle() == null;
        }
    }

    public boolean isExecutableState(PlayerPatch<?> executor) {
        if(!executor.isLogicalClient()){
            return !executor.isUnstable() && !executor.getOriginal().isSpectator();
        }
        return executor.getEntityState().canBasicAttack() && !executor.isUnstable() && !executor.getOriginal().isSpectator();
    }

    /**
     * 处理客户端的输入信息
     * 处理输入位于{@link InputManager#getExecutionPacket(SkillContainer)}
     */
    @Override
    public void executeOnServer(ServerPlayerPatch executor, FriendlyByteBuf args) {
        ComboType type = ComboType.ENUM_MANAGER.get(args.readInt());
        if (type == null) {
            return;
        }
        if (executor.getOriginal().getMainHandItem().is(InvincibleItems.DEBUG.get())) {
            System.out.println(executor.getOriginal().getMainHandItem().getDescriptionId() + " " + type);
        }
        executor.getOriginal().getCapability(InvincibleCapabilityProvider.INVINCIBLE_PLAYER).ifPresent(invinciblePlayer -> {
            ComboNode last = invinciblePlayer.getCurrentNode();
            if(last == null){
                return;
            }
            ComboNode current = last.getNext(type);
            ComboNode next = current;
            //如果是空的，则尝试子输入，防止不小心按到多个按键的情况
            if(current == null){
                for(ComboType subType : type.getSubTypes()){
                    if((current = last.getNext(subType)) != null){
                        break;
                    }
                }
            }
            //动画是空的就直接跳过，不是就播放
            if (current != null) {
                if (current.getAnimationProvider() == null || !current.getConditionAnimations().isEmpty()) {
                    if (current.getConditionAnimations().isEmpty()) {
                        return;
                    }
                    current.getConditionAnimations().sort(Comparator.comparingInt(ComboNode::getPriority).reversed());
                    //多个条件指向不同动画，根据优先级来检测
                    for (ComboNode conditionAnimation : current.getConditionAnimations()) {
                        boolean canExecute = true;
                        for (Condition condition : conditionAnimation.getConditions()) {
                            if (!condition.predicate(executor)) {
                                canExecute = false;
                                break;
                            }
                        }
                        if (canExecute) {
                            current = conditionAnimation;
                            //实现ConditionAnimations里接combos
                            if (conditionAnimation.hasNext()) {
                                next = conditionAnimation;
                            }
                            break;
                        }
                    }
                } else {
                    //多个条件指向同一动画
                    for (Condition condition : current.getConditions()) {
                        if (!condition.predicate(executor)) {
                            return;
                        }
                    }
                }
                StaticAnimation animation = current.getAnimation();
                if (animation == null) {
                    return;
                }
                float convertTime = current.getConvertTime();
                executor.playAnimationSynchronized(animation, convertTime);
                initPlayer(executor.getSkill(SkillSlots.WEAPON_INNATE), invinciblePlayer, current);
                //把玩家参数同步给客户端
                SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(executor.getSkill(this).getSlotId());
                feedbackPacket.getBuffer().writeNbt(invinciblePlayer.saveNBTData(new CompoundTag()));
                EpicFightNetworkManager.sendToPlayer(feedbackPacket, executor.getOriginal());
                invinciblePlayer.setCurrentNode(next);
            } else {
                invinciblePlayer.setCurrentNode(root);
            }
        });

    }

    /**
     * 根据预存来初始化玩家信息
     */
    private void initPlayer(SkillContainer container, InvinciblePlayer invinciblePlayer, ComboNode next) {
        invinciblePlayer.clearTimeEvents();
        for (TimeStampedEvent event : next.getTimeEvents()) {
            event.resetExecuted();
            invinciblePlayer.addTimeEvent(event);
        }
        invinciblePlayer.setHurtEvents(ImmutableList.copyOf(next.getHurtEvents()));
        invinciblePlayer.setHitSuccessEvents(ImmutableList.copyOf(next.getHitEvents()));
        invinciblePlayer.setDodgeSuccessEvents(ImmutableList.copyOf(next.getDodgeSuccessEvents()));
        invinciblePlayer.setCanBeInterrupt(next.isCanBeInterrupt());
        invinciblePlayer.setPlaySpeedMultiplier(next.getPlaySpeed());
        invinciblePlayer.setNotCharge(next.isNotCharge());
        invinciblePlayer.setPhase(next.getNewPhase());
        if(next.getCooldown() > 0){
            container.getDataManager().setDataSync(COOLDOWN_TIMER, next.getCooldown(), ((ServerPlayerPatch) container.getExecuter()).getOriginal());
            invinciblePlayer.setItemCooldown(container.getExecuter().getOriginal().getMainHandItem(), next.getCooldown());

        }
        invinciblePlayer.setArmorNegation(next.getArmorNegation());
        invinciblePlayer.setHurtDamageMultiplier(next.getHurtDamageMultiplier());
        invinciblePlayer.setDamageMultiplier(next.getDamageMultiplier());
        invinciblePlayer.setImpactMultiplier(next.getImpactMultiplier());
        invinciblePlayer.setStunTypeModifier(next.getStunTypeModifier());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeOnClient(LocalPlayerPatch executor, FriendlyByteBuf args) {
        CompoundTag tag = args.readNbt();
        if (tag != null) {
            InvincibleCapabilityProvider.get(executor.getOriginal()).loadNBTData(tag);
        }
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        container.getDataManager().registerData(DODGE_SUCCESS_TIMER);
        container.getDataManager().registerData(COOLDOWN_TIMER);
        container.getDataManager().registerData(PARRY_TIMER);
        container.getDataManager().registerData(UP);
        container.getDataManager().registerData(DOWN);
        container.getDataManager().registerData(LEFT);
        container.getDataManager().registerData(RIGHT);

        //初始化连段
        if (!container.getExecuter().isLogicalClient()) {
            resetCombo(((ServerPlayerPatch) container.getExecuter()), root);
        }
        InvincibleCapabilityProvider.get(container.getExecuter().getOriginal()).resetPhase();

        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID, (event -> {
            ImmutableList<BiEvent> dodgeSuccessEvents = InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getDodgeSuccessEvents();
            if(dodgeSuccessEvents != null){
                dodgeSuccessEvents.forEach(dodgeEvent -> dodgeEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget()));
            }
            container.getDataManager().setDataSync(DODGE_SUCCESS_TIMER, Config.EFFECT_TICK.get(), event.getPlayerPatch().getOriginal());
        }));
        //减伤和霸体的判断
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID, (event -> {
            InvinciblePlayer invinciblePlayer = InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal());
            if (event.getDamageSource() instanceof EpicFightDamageSource epicFightDamageSource && !invinciblePlayer.isCanBeInterrupt()) {
                epicFightDamageSource.setStunType(StunType.NONE);
            }
            if (invinciblePlayer.getHurtDamageMultiplier() != 0) {
                event.setAmount(event.getAmount() * invinciblePlayer.getHurtDamageMultiplier());
            }
            //招架成功的判断
            if(event.isParried()){
                container.getDataManager().setDataSync(PARRY_TIMER, Config.EFFECT_TICK.get(), event.getPlayerPatch().getOriginal());
            }
        }));
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.HURT_EVENT_POST, EVENT_UUID, (event -> {
            ImmutableList<BiEvent> hurtEvents = InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getHurtEvents();
            if(hurtEvents != null){
                hurtEvents.forEach(hurtEvent -> hurtEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget()));
            }
        }));
        //调整攻击倍率，冲击，硬直类型等
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.DEALT_DAMAGE_EVENT_PRE, EVENT_UUID, (event -> {
            InvinciblePlayer invinciblePlayer = InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal());
            if (invinciblePlayer.getStunTypeModifier() != null) {
                event.getDamageSource().setStunType(invinciblePlayer.getStunTypeModifier());
            }
            if (invinciblePlayer.getImpactMultiplier() != 0) {
                event.getDamageSource().setImpact(invinciblePlayer.getImpactMultiplier());
            }
            if(invinciblePlayer.getArmorNegation() != 0){
                event.getDamageSource().setArmorNegation(invinciblePlayer.getArmorNegation());
            }
            if (invinciblePlayer.getDamageMultiplier() != null) {
                event.getDamageSource().setDamageModifier(invinciblePlayer.getDamageMultiplier());
            }
        }));
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.DEALT_DAMAGE_EVENT_POST, EVENT_UUID, (event -> {
            ImmutableList<BiEvent> hitEvents = InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getHitSuccessEvents();
            if(hitEvents != null){
                hitEvents.forEach(hitEvent -> hitEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget()));
            }
        }));
        //取消原版的普攻和跳攻
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (event -> {
            //不影响默认的普攻
            ItemStack mainHandItem = event.getPlayerPatch().getOriginal().getMainHandItem();
            if(mainHandItem.isEmpty() || !mainHandItem.getCapability(EpicFightCapabilities.CAPABILITY_ITEM).isPresent()){
                return;
            }
            SkillCategory skillCategory = event.getSkillContainer().getSkill().getCategory();
            if (skillCategory.equals(SkillCategories.BASIC_ATTACK) && !event.getPlayerPatch().getOriginal().isPassenger() || skillCategory.equals(SkillCategories.AIR_ATTACK)) {
                event.setCanceled(true);
            }
        }));
        //播放walk的过渡动画
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event -> {
            Input input = event.getMovementInput();
            boolean isUp = input.up;
            if (isUp && !isWalking) {
                if (walkBegin != null) {
                    container.getExecuter().playAnimationSynchronized(walkBegin.get(), 0.15F);
                }
                isWalking = true;
            }
            if (!isUp && isWalking) {
                if (walkEnd != null) {
                    container.getExecuter().playAnimationSynchronized(walkEnd.get(), 0.15F);
                }
                isWalking = false;
            }
        }));
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.HURT_EVENT_POST, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.DEALT_DAMAGE_EVENT_PRE, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.DEALT_DAMAGE_EVENT_POST, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
    }

    /**
     * 超时重置
     */
    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);
        if (!container.getExecuter().isLogicalClient() && container.getExecuter().getTickSinceLastAction() > Config.RESET_TICK.get()) {
            resetCombo(((ServerPlayerPatch) container.getExecuter()), root);
        }

        InvinciblePlayer invinciblePlayer = InvincibleCapabilityProvider.get(container.getExecuter().getOriginal());

        SkillDataManager manager = container.getDataManager();
        if (manager.getDataValue(DODGE_SUCCESS_TIMER) > 0) {
            manager.setData(DODGE_SUCCESS_TIMER, manager.getDataValue(DODGE_SUCCESS_TIMER) - 1);
        }
        if (container.getExecuter() instanceof ServerPlayerPatch serverPlayerPatch) {
            ItemStack itemStack = serverPlayerPatch.getOriginal().getMainHandItem();
            int currentCooldown = invinciblePlayer.getItemCooldown(itemStack);
            if (currentCooldown > 0) {
                currentCooldown = currentCooldown - 1;
                invinciblePlayer.setItemCooldown(itemStack, currentCooldown);
            }
            if (currentCooldown != manager.getDataValue(COOLDOWN_TIMER)) {
                manager.setDataSync(COOLDOWN_TIMER, currentCooldown, serverPlayerPatch.getOriginal());
            }
        }
        if (manager.getDataValue(PARRY_TIMER) > 0) {
            manager.setData(PARRY_TIMER, manager.getDataValue(PARRY_TIMER) - 1);
        }
    }

    public static void resetCombo(ServerPlayerPatch serverPlayerPatch, ComboNode root) {
        InvinciblePlayer invinciblePlayer = InvincibleCapabilityProvider.get(serverPlayerPatch.getOriginal());
        invinciblePlayer.setCurrentNode(root);
        invinciblePlayer.clear();
        //借他的包同步数据给客户端
        SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(SkillSlots.WEAPON_INNATE.universalOrdinal());
        feedbackPacket.getBuffer().writeNbt(invinciblePlayer.saveNBTData(new CompoundTag()));
        EpicFightNetworkManager.sendToPlayer(feedbackPacket, serverPlayerPatch.getOriginal());
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        return shouldDrawGui;
    }

    public boolean isShouldDrawGui() {
        return shouldDrawGui;
    }

    public static class Builder extends Skill.Builder<ComboBasicAttack> {
        protected ComboNode root;

        @Nullable
        protected StaticAnimationProvider walkBegin, walkEnd;

        protected boolean shouldDrawGui;

        public Builder() {
        }

        public Builder setCategory(SkillCategory category) {
            this.category = category;
            return this;
        }

        public Builder setActivateType(Skill.ActivateType activateType) {
            this.activateType = activateType;
            return this;
        }

        public Builder setResource(Skill.Resource resource) {
            this.resource = resource;
            return this;
        }

        public Builder setCombo(ComboNode root) {
            this.root = root;
            return this;
        }

        public Builder setShouldDrawGui(boolean shouldDrawGui) {
            this.shouldDrawGui = shouldDrawGui;
            return this;
        }

        public Builder setWalkBeginAnim(StaticAnimationProvider walkBegin) {
            this.walkBegin = walkBegin;
            return this;
        }

        public Builder setWalkEndAnim(StaticAnimationProvider walkEnd) {
            this.walkEnd = walkEnd;
            return this;
        }
    }

}
