package com.p1nero.invincible.skill;

import com.p1nero.invincible.Config;
import com.p1nero.invincible.animation.StaticAnimationProvider;
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
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.Comparator;
import java.util.UUID;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ComboBasicAttack extends Skill {

    protected static final UUID EVENT_UUID = UUID.fromString("d1d114cc-f11f-11ed-a05b-0242ac114514");
    public static SkillDataManager.SkillDataKey<Integer> DODGE_SUCCESS_TIMER = SkillDataManager.SkillDataKey.createDataKey(SkillDataManager.ValueType.INTEGER);
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

    @Override
    public boolean isExecutableState(PlayerPatch<?> executor) {
        return executor.getEntityState().canBasicAttack() && !executor.getOriginal().isSpectator();
    }

    /**
     * 处理客户端的输入信息，原谅我无脑if偷懒
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
            ComboNode current = invinciblePlayer.getCurrentNode();
            ComboNode next = current.getNext(type);
            //如果是空的，则尝试子输入，防止不小心按到多个按键的情况
            if(next == null){
                for(ComboType subType : type.getSubTypes()){
                    if((next = current.getNext(subType)) != null){
                        break;
                    }
                }
            }
            //动画是空的就直接跳过，不是就播放
            if (next != null) {
                StaticAnimation animation = null;
                float convertTime = 0.0F;
                if (next.getAnimationProvider() == null || !next.getConditionAnimations().isEmpty()) {
                    if (next.getConditionAnimations().isEmpty()) {
                        return;
                    }
                    next.getConditionAnimations().sort(Comparator.comparingInt(ComboNode::getPriority).reversed());
                    //多个条件指向不同动画，根据优先级来检测
                    for (ComboNode conditionAnimation : next.getConditionAnimations()) {
                        boolean canExecute = true;
                        for (Condition condition : conditionAnimation.getConditions()) {
                            if (!condition.predicate(executor)) {
                                canExecute = false;
                                break;
                            }
                        }
                        if (canExecute) {
                            animation = conditionAnimation.getAnimation();
                            convertTime = conditionAnimation.getConvertTime();
                            //实现ConditionAnimations里接combos
                            if (conditionAnimation.hasNext()) {
                                next = conditionAnimation;
                            }
                            break;
                        }
                    }
                } else {
                    //多个条件指向同一动画
                    for (Condition condition : next.getConditions()) {
                        if (!condition.predicate(executor)) {
                            return;
                        }
                    }
                    animation = next.getAnimation();
                    convertTime = next.getConvertTime();
                }
                SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(executor.getSkill(this).getSlotId());
                feedbackPacket.getBuffer().writeNbt(invinciblePlayer.saveNBTData(new CompoundTag()));
                EpicFightNetworkManager.sendToPlayer(feedbackPacket, executor.getOriginal());
                if (animation == null) {
                    return;
                }
                executor.playAnimationSynchronized(animation, convertTime);
                initPlayer(invinciblePlayer, next);
                invinciblePlayer.setCurrentNode(next);
            } else {
                invinciblePlayer.setCurrentNode(root);
            }
        });

    }

    /**
     * 根据预存来初始化玩家信息
     */
    private void initPlayer(InvinciblePlayer invinciblePlayer, ComboNode next) {
        invinciblePlayer.clearTimeEvents();
        for (TimeStampedEvent event : next.getTimeEvents()) {
            event.resetExecuted();
            invinciblePlayer.addTimeEvent(event);
        }
        for (BiEvent event : next.getHurtEvents()) {
            invinciblePlayer.addHurtEvent(event);
        }
        for (BiEvent event : next.getHitEvents()) {
            invinciblePlayer.addHitSuccessEvent(event);
        }
        for (BiEvent event : next.getDodgeSuccessEvents()) {
            invinciblePlayer.addDodgeSuccessEvent(event);
        }
        invinciblePlayer.setCanBeInterrupt(next.isCanBeInterrupt());
        invinciblePlayer.setPlaySpeedMultiplier(next.getPlaySpeed());
        invinciblePlayer.setNotCharge(next.isNotCharge());
        invinciblePlayer.setPhase(next.getNewPhase());
        invinciblePlayer.setCooldown(next.getCooldown());
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
        container.getDataManager().registerData(PARRY_TIMER);
        container.getDataManager().registerData(UP);
        container.getDataManager().registerData(DOWN);
        container.getDataManager().registerData(LEFT);
        container.getDataManager().registerData(RIGHT);
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID, (event -> {
            for (BiEvent hurtEvent : InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getDodgeSuccessEvents()) {
                hurtEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget());
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
            for (BiEvent hurtEvent : InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getHurtEvents()) {
                hurtEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget());
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
            if (invinciblePlayer.getDamageMultiplier() != null) {
                event.getDamageSource().setDamageModifier(invinciblePlayer.getDamageMultiplier());
            }
        }));
        //自己写个充能用
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.DEALT_DAMAGE_EVENT_POST, EVENT_UUID, (event -> {
            if (!InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal()).isNotCharge()) {
                if (!container.isFull()) {
                    float value = container.getResource() + event.getAttackDamage();
                    if (value > 0.0F) {
                        this.setConsumptionSynchronize(event.getPlayerPatch(), value);
                    }
                }
            }
            for (BiEvent hurtEvent : InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getHitSuccessEvents()) {
                hurtEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget());
            }
        }));
        //取消原版的普攻和跳攻
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (event -> {
            SkillCategory skillCategory = event.getSkillContainer().getSkill().getCategory();
            if (skillCategory.equals(SkillCategories.BASIC_ATTACK) && !event.getPlayerPatch().getOriginal().isPassenger() || skillCategory.equals(SkillCategories.AIR_ATTACK)) {
                event.setCanceled(true);
            }
        }));
        //初始化连段
        if (!container.getExecuter().isLogicalClient()) {
            resetCombo(((ServerPlayerPatch) container.getExecuter()), root);
        }
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
        InvincibleCapabilityProvider.get(container.getExecuter().getOriginal()).tick();
        SkillDataManager manager = container.getDataManager();
        if(manager.hasData(DODGE_SUCCESS_TIMER)){
            manager.setData(DODGE_SUCCESS_TIMER, Math.max(manager.getDataValue(DODGE_SUCCESS_TIMER) - 1, 0));
        }
        if(manager.hasData(PARRY_TIMER)){
            manager.setData(PARRY_TIMER, Math.max(manager.getDataValue(PARRY_TIMER) - 1, 0));
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
