package com.p1nero.invincible.skill;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.p1nero.invincible.Config;
import com.p1nero.invincible.conditions.PressedTimeCondition;
import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.attachment.InvincibleAttachments;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.attachment.InvinciblePlayer;
import com.p1nero.invincible.client.events.InputManager;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.item.InvincibleItems;
import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.api.combo.ComboType;
import net.minecraft.client.player.Input;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.neoforgeevent.playerpatch.*;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSkillFeedback;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ComboBasicAttack extends Skill {

    public static final Logger LOGGER = LogUtils.getLogger();
    protected static final UUID EVENT_UUID = UUID.fromString("d1d114cc-f11f-11ed-a05b-0242ac114514");

    @OnlyIn(Dist.CLIENT)
    protected boolean isWalking;
    protected boolean shouldDrawGui;
    protected List<String> translationKeys;
    protected int maxPressTime, maxReserveTime, maxProtectTime;

    @Nullable
    protected AnimationManager.AnimationAccessor<? extends StaticAnimation> walkBegin, walkEnd;

    protected ComboNode root;

    public ComboBasicAttack(Builder builder) {
        super(builder);
        this.shouldDrawGui = builder.shouldDrawGui;
        this.root = builder.root;
        this.walkBegin = builder.walkBegin;
        this.walkEnd = builder.walkEnd;
        this.translationKeys = builder.translationKeys;
        maxPressTime = builder.maxPressTime;
        maxReserveTime = builder.maxReserveTime;
        maxProtectTime = builder.maxProtectTime;
    }

    public static Builder createComboBasicAttack(Function<ComboBasicAttack.Builder, ComboBasicAttack> constructor) {
        return new Builder(constructor).setCategory(SkillCategories.WEAPON_INNATE).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.NONE);
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        if (container.getExecutor().isLogicalClient()) {
            return super.canExecute(container);
        } else {
            ItemStack itemstack = container.getExecutor().getOriginal().getMainHandItem();
            return super.canExecute(container) && EpicFightCapabilities.getItemStackCapability(itemstack).getInnateSkill(container.getExecutor(), itemstack) == this && container.getExecutor().getOriginal().getVehicle() == null;
        }
    }

    @Override
    public boolean isExecutableState(PlayerPatch<?> executor) {
        return executor.getEntityState().canBasicAttack() && !executor.getOriginal().isSpectator();
    }

    /**
     * 处理客户端的输入信息
     * 处理输入位于{@link InputManager#getExecutionPacket(SkillContainer)}
     */
    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        ComboType type = ComboType.ENUM_MANAGER.get(args.readInt());
        if (type == null) {
            return;
        }
        this.executeOnServer(container, type, args.readInt());
    }

    /**
     * 方便额外调用
     * pressedTime不为0，防止和原版的技能键冲突。
     */
    public void executeOnServer(SkillContainer container, ComboType type, int pressedTime){
        if(pressedTime > getMaxProtectTime() || pressedTime == 0) {
            return;
        }
        boolean debugMode = container.getExecutor().getOriginal().getMainHandItem().is(InvincibleItems.DEBUG.get()) || container.getExecutor().getOriginal().getMainHandItem().is(InvincibleItems.DATAPACK_DEBUG.get());
        if (debugMode) {
            LOGGER.debug("{} {} : pressed {} ticks.", container.getExecutor().getOriginal().getMainHandItem().getDescriptionId(), type, pressedTime);
        }
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.get(container.getExecutor().getOriginal());
        ComboNode last = invinciblePlayer.getCurrentNode();
        boolean hasPressedTimeCondition = false;
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
                        if(condition instanceof PressedTimeCondition pressedTimeCondition) {
                            if(pressedTime < pressedTimeCondition.getMin() || pressedTime > pressedTimeCondition.getMax()) {
                                canExecute = false;
                                break;
                            }
                        } else if (!condition.predicate(container.getExecutor())) {
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
                    if(condition instanceof PressedTimeCondition pressedTimeCondition) {
                        hasPressedTimeCondition = true;
                        if(pressedTime < pressedTimeCondition.getMin() || pressedTime > pressedTimeCondition.getMax()) {
                            break;
                        }
                    } else if (!condition.predicate(container.getExecutor())) {
                        return;
                    }
                }
                if(!hasPressedTimeCondition && pressedTime > 20) {
                    return;
                }
            }
            AnimationManager.AnimationAccessor animationAccessor = current.getAnimationAccessor();
            if (animationAccessor == null) {
                return;
            }
            float convertTime = current.getConvertTime();
            if (debugMode) {
                LOGGER.debug("animationAccessor: {}", animationAccessor);
            }
            container.getExecutor().playAnimationSynchronized(animationAccessor, convertTime);
            initPlayer(container, invinciblePlayer, current);
            //把玩家参数同步给客户端
            SPSkillFeedback feedbackPacket = SPSkillFeedback.executed(container.getSlot());
            feedbackPacket.buffer().writeNbt(invinciblePlayer.saveNBTData(new CompoundTag()));
            EpicFightNetworkManager.sendToPlayer(feedbackPacket, (ServerPlayer) container.getExecutor().getOriginal());
            invinciblePlayer.setCurrentNode(next);
        } else {
            invinciblePlayer.setCurrentNode(root);
        }
    }

    public static void executeOnServer(ServerPlayer serverPlayer, ComboType type){
        executeOnServer(serverPlayer, type, 1);
    }

    public static void executeOnServer(ServerPlayer serverPlayer, ComboType type, int pressedTime){
        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(serverPlayer, ServerPlayerPatch.class);
        if(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack comboBasicAttack){
            comboBasicAttack.executeOnServer(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), type, pressedTime);
        }
    }

    /**
     * 根据预存来初始化玩家信息
     */
    private void initPlayer(SkillContainer container, InvinciblePlayer invinciblePlayer, ComboNode next) {
        invinciblePlayer.resetTimeEvents();
        ImmutableList.Builder builder = ImmutableList.<TimeStampedEvent>builder();
        for (TimeStampedEvent event : next.getTimeEvents()) {
            event.resetExecuted();
            builder.add(event);
        }
        invinciblePlayer.setTimeStampedEvents(builder.build());
        invinciblePlayer.setHurtEvents(ImmutableList.copyOf(next.getHurtEvents()));
        invinciblePlayer.setHitSuccessEvents(ImmutableList.copyOf(next.getHitEvents()));
        invinciblePlayer.setDodgeSuccessEvents(ImmutableList.copyOf(next.getDodgeSuccessEvents()));
        invinciblePlayer.setCanBeInterrupt(next.isCanBeInterrupt());
        invinciblePlayer.setPlaySpeedMultiplier(next.getPlaySpeed());
        invinciblePlayer.setNotCharge(next.isNotCharge());
        invinciblePlayer.setPhase(next.getNewPhase());

        if(next.getCooldown() > 0){
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.COOLDOWN, next.getCooldown(), ((ServerPlayer) container.getExecutor().getOriginal()));
            invinciblePlayer.setItemCooldown(container.getExecutor().getOriginal().getMainHandItem(), next.getCooldown());
        }

        invinciblePlayer.setArmorNegation(next.getArmorNegation());
        invinciblePlayer.setHurtDamageMultiplier(next.getHurtDamageMultiplier());
        invinciblePlayer.setDamageMultiplier(next.getDamageMultiplier());
        invinciblePlayer.setImpactMultiplier(next.getImpactMultiplier());
        invinciblePlayer.setStunTypeModifier(next.getStunTypeModifier());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
        CompoundTag tag = args.readNbt();
        if (tag != null) {
            InvincibleAttachments.get(container.getExecutor().getOriginal()).loadNBTData(tag);
        }
    }

    /**
     * 闪避成功事件的处理，以及闪避条件
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onDodgeSuccess(DodgeSuccessEvent event, SkillContainer container) {
        ImmutableList<BiEvent> dodgeSuccessEvents = InvincibleAttachments.get(event.getPlayerPatch().getOriginal()).getDodgeSuccessEvents();
        if(dodgeSuccessEvents != null){
            dodgeSuccessEvents.forEach(dodgeEvent -> dodgeEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget()));
        }
        container.getDataManager().setDataSync(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER, Config.EFFECT_TICK.get(), event.getPlayerPatch().getOriginal());
    }

    /**
     * 减伤和霸体的判断
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onHurtEventPre(TakeDamageEvent.Pre event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.get(event.getPlayerPatch().getOriginal());
        if (event.getDamageSource() instanceof EpicFightDamageSource epicFightDamageSource && !invinciblePlayer.isCanBeInterrupt()) {
            epicFightDamageSource.setStunType(StunType.NONE);
        }
        if (invinciblePlayer.getHurtDamageMultiplier() != 0) {
            event.setDamage(event.getOriginalDamage() * invinciblePlayer.getHurtDamageMultiplier());
        }
        //招架成功的判断，配合优先级-1使用
        if(event.isParried()){
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.PARRY_TIMER, Config.EFFECT_TICK.get(), event.getPlayerPatch().getOriginal());
        }
    }

    /**
     * 抛出受伤事件
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onHurtEventPost(TakeDamageEvent.Post event, SkillContainer container) {
        ImmutableList<BiEvent> hurtEvents = InvincibleAttachments.get(event.getPlayerPatch().getOriginal()).getHurtEvents();
        if(hurtEvents != null){
            hurtEvents.forEach(hurtEvent -> hurtEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget()));
        }
    }

    /**
     * 调整攻击倍率，冲击，硬直类型等
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onDealDamageEventPre(DealDamageEvent.Pre event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.get(event.getPlayerPatch().getOriginal());
        if (invinciblePlayer.getStunTypeModifier() != null) {
            event.getDamageSource().setStunType(invinciblePlayer.getStunTypeModifier());
        }
        if (invinciblePlayer.getImpactMultiplier() != 1.0F) {
            event.getDamageSource().setImpact(event.getDamageSource().getImpact() * invinciblePlayer.getImpactMultiplier());
        }
        if(invinciblePlayer.getArmorNegation() != 0){
            event.getDamageSource().setArmorNegation(invinciblePlayer.getArmorNegation());
        }
        if (invinciblePlayer.getDamageMultiplier() != null) {
            event.getDamageSource().setDamageModifier(invinciblePlayer.getDamageMultiplier());
        }
    }

    /**
     * 自己的充能
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onDealDamageEventPost(DealDamageEvent.Post event, SkillContainer container) {
        if (!InvincibleAttachments.get(event.getPlayerPatch().getOriginal()).isNotCharge()) {
            if (!container.isFull()) {
                float value = container.getResource() + event.getNeoForgeEvent().getNewDamage();
                if (value > 0.0F) {
                    this.setConsumptionSynchronize(container, value);
                }
            }
        }
        ImmutableList<BiEvent> hitEvents = InvincibleAttachments.get(event.getPlayerPatch().getOriginal()).getHitSuccessEvents();
        if(hitEvents != null){
            hitEvents.forEach(hitEvent -> hitEvent.testAndExecute(event.getPlayerPatch(), event.getTarget() == null ? event.getPlayerPatch().getTarget() : event.getTarget()));
        }
    }

    /**
     * 取消原版的普攻和跳攻
     */
    @SkillEvent(side = SkillEvent.Side.BOTH)
    public void onSkillExecute(SkillExecuteEvent event, SkillContainer container) {
        //不影响默认的普攻
        ItemStack mainHandItem = event.getPlayerPatch().getOriginal().getMainHandItem();
        if(mainHandItem.isEmpty() || EpicFightCapabilities.getItemCapability(mainHandItem).isEmpty()){
            return;
        }
        //不影响没技能但是有模板的武器
        CapabilityItem capabilityItem = EpicFightCapabilities.getItemStackCapability(mainHandItem);
        if(capabilityItem == null || capabilityItem.getInnateSkill(event.getPlayerPatch(), mainHandItem) == null) {
            return;
        }
        SkillCategory skillCategory = event.getSkillContainer().getSkill().getCategory();
        if (skillCategory.equals(SkillCategories.BASIC_ATTACK) && !event.getPlayerPatch().getOriginal().isPassenger() || skillCategory.equals(SkillCategories.AIR_ATTACK)) {
            event.setCanceled(true);
        }
    }

    /**
     * 播放walk的过渡动画
     */
    @SkillEvent(side = SkillEvent.Side.CLIENT)
    public void onMovementInput(MovementInputUpdateEvent event, SkillContainer container) {
        Input input = event.getInput();
        boolean isUp = input.up;
        if (isUp && !isWalking) {
            if (walkBegin != null) {
                container.getExecutor().playAnimationSynchronized(walkBegin, 0.15F);
            }
            isWalking = true;
        }
        if (!isUp && isWalking) {
            if (walkEnd != null) {
                container.getExecutor().playAnimationSynchronized(walkEnd, 0.15F);
            }
            isWalking = false;
        }
    }



    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        //初始化连段
        if (!container.getExecutor().isLogicalClient()) {
            resetCombo(((ServerPlayerPatch) container.getExecutor()), root);
        }
        InvincibleAttachments.get(container.getExecutor().getOriginal()).resetPhase();
        container.getDataManager().setData(InvincibleSkillDataKeys.COOLDOWN, 0);
    }

    /**
     * 超时重置
     */
    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);
        if (!container.getExecutor().isLogicalClient() && container.getExecutor().getTickSinceLastAction() > Config.RESET_TICK.get()) {
            resetCombo(((ServerPlayerPatch) container.getExecutor()), root);
        }
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.get(container.getExecutor().getOriginal());
        SkillDataManager manager = container.getDataManager();
        if(manager.hasData(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER)){
            manager.setData(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER, Math.max(manager.getDataValue(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER) - 1, 0));
        }
        if(manager.hasData(InvincibleSkillDataKeys.PARRY_TIMER)){
            manager.setData(InvincibleSkillDataKeys.PARRY_TIMER, Math.max(manager.getDataValue(InvincibleSkillDataKeys.PARRY_TIMER) - 1, 0));
        }
        if (container.getExecutor() instanceof ServerPlayerPatch serverPlayerPatch) {
            ItemStack itemStack = serverPlayerPatch.getOriginal().getMainHandItem();
            int currentCooldown = invinciblePlayer.getItemCooldown(itemStack);
            if (currentCooldown > 0) {
                currentCooldown = currentCooldown - 1;
                invinciblePlayer.setItemCooldown(itemStack, currentCooldown);
            }
            if (currentCooldown != manager.getDataValue(InvincibleSkillDataKeys.COOLDOWN)) {
                manager.setDataSync(InvincibleSkillDataKeys.COOLDOWN, currentCooldown, serverPlayerPatch.getOriginal());
            }
        }
    }

    public void resetCombo(ServerPlayerPatch serverPlayerPatch, ComboNode root) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.get(serverPlayerPatch.getOriginal());
        invinciblePlayer.setCurrentNode(root);
        invinciblePlayer.clear();
        //借他的包同步数据给客户端
        SPSkillFeedback feedbackPacket = SPSkillFeedback.executed(SkillSlots.WEAPON_INNATE);
        feedbackPacket.buffer().writeNbt(invinciblePlayer.saveNBTData(new CompoundTag()));
        EpicFightNetworkManager.sendToPlayer(feedbackPacket, serverPlayerPatch.getOriginal());
    }

    @Override
    public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
        if(translationKeys.isEmpty()){
            return super.getTooltipOnItem(itemStack, cap, playerpatch);
        }
        List<Component> list = Lists.newArrayList();
        for(String translationKey : translationKeys){
            list.add(Component.translatable(translationKey));
        }
        return list;
    }

    public int getMaxPressTime() {
        return maxPressTime == 0 ? Config.MAX_PRESS_TICK.get() : maxPressTime;
    }

    public int getMaxProtectTime() {
        return maxProtectTime == 0 ? Config.PRESS_PROTECT_TICK.get() : maxProtectTime;
    }

    public int getMaxReserveTime() {
        return maxReserveTime == 0 ? Config.RESERVE_TICK.get() : maxReserveTime;
    }


    @Override
    public boolean shouldDraw(SkillContainer container) {
        return shouldDrawGui;
    }

    public static class Builder extends SkillBuilder<Builder> {
        protected ComboNode root;

        protected List<String> translationKeys = List.of();

        @Nullable
        protected AnimationManager.AnimationAccessor<? extends StaticAnimation> walkBegin, walkEnd;

        protected boolean shouldDrawGui;
        protected int maxPressTime, maxReserveTime, maxProtectTime;

        public Builder(Function<Builder, ? extends ComboBasicAttack> constructor) {
            super(constructor);
        }

        public Builder setMaxPressTime(int maxPressTime) {
            this.maxPressTime = maxPressTime;
            return this;
        }

        public Builder setMaxProtectTime(int maxProtectTime) {
            this.maxProtectTime = maxProtectTime;
            return this;
        }

        public Builder setReserveTime(int maxReserveTime) {
            this.maxReserveTime = maxReserveTime;
            return this;
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

        public Builder setWalkBeginAnim(AnimationManager.AnimationAccessor<? extends StaticAnimation> walkBegin) {
            this.walkBegin = walkBegin;
            return this;
        }

        public Builder setWalkEndAnim(AnimationManager.AnimationAccessor<? extends StaticAnimation> walkEnd) {
            this.walkEnd = walkEnd;
            return this;
        }

        public Builder addToolTipOnItem(List<String> translationKeys) {
            this.translationKeys = translationKeys;
            return this;
        }

    }

}
