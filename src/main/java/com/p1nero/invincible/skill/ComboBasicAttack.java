package com.p1nero.invincible.skill;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.p1nero.invincible.Config;
import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.api.events.Side;
import com.p1nero.invincible.capability.InvinciblePlayerCapabilityProvider;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.capability.InvinciblePlayer;
import com.p1nero.invincible.client.InputManager;
import com.p1nero.invincible.conditions.PressIntervalCondition;
import com.p1nero.invincible.conditions.PressedTimeCondition;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.item.InvincibleItems;
import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.api.skill.ComboType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.data.conditions.Condition;
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

import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ComboBasicAttack extends AbstractInvincibleInnateSkill {

    protected static final UUID EVENT_UUID = UUID.fromString("d1d114cc-f11f-11ed-a05b-0242ac114514");

    public static final Logger LOGGER = LogUtils.getLogger();

    @OnlyIn(Dist.CLIENT)
    protected boolean isWalking;
    protected boolean shouldDrawGui;
    protected ResourceLocation skillTextureLocation;
    protected List<String> translationKeys;

    @Nullable
    protected AnimationManager.AnimationAccessor<? extends StaticAnimation> walkBegin, walkEnd;

    protected ComboNode root;
    protected int maxPressTime, maxReserveTime, maxProtectTime;

    public ComboBasicAttack(Builder builder) {
        super(builder);
        this.shouldDrawGui = builder.shouldDrawGui;
        this.skillTextureLocation = builder.skillTextureLocation;
        this.root = builder.root;
        this.walkBegin = builder.walkBegin;
        this.walkEnd = builder.walkEnd;
        this.translationKeys = builder.translationKeys;
        maxPressTime = builder.maxPressTime;
        maxReserveTime = builder.maxReserveTime;
        maxProtectTime = builder.maxProtectTime;
    }

    public static Builder createComboBasicAttack() {
        return new Builder().setCategory(SkillCategories.WEAPON_INNATE).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.NONE);
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
     * 处理输入位于{@link InputManager#getAvailablePackets(SkillContainer)}
     */
    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        ComboType type = ComboType.ENUM_MANAGER.get(args.readInt());
        if (type == null) {
            return;
        }
        int pressedTime = args.readInt();
        long pressInterval = args.readLong();
        this.executeOnServer(container, type, pressedTime, pressInterval);
    }

    /**
     * 方便额外调用
     */
    public void executeOnServer(SkillContainer container, ComboType type, int pressedTime, long inputInterval) {
        if (pressedTime > getMaxProtectTime()) {
            return;
        }
        boolean debugMode = container.getExecutor().getOriginal().getMainHandItem().is(InvincibleItems.DEBUG.get()) || container.getExecutor().getOriginal().getMainHandItem().is(InvincibleItems.CUSTOM_COMBO_DEMO.get());
        if (debugMode) {
            LOGGER.debug("{} {} : pressed {} ticks. Interval: {} ms.", container.getExecutor().getOriginal().getMainHandItem().getDescriptionId(), type, pressedTime, inputInterval);
        }
        container.getExecutor().getOriginal().getCapability(InvinciblePlayerCapabilityProvider.INVINCIBLE_PLAYER).ifPresent(invinciblePlayer -> {
            ComboNode last = invinciblePlayer.getCurrentNode();
            boolean hasPressedTimeCondition = false;
            if (last == null) {
                return;
            }
            ComboNode current = last.getNext(type);
            ComboNode next = current;
            //如果是空的，则尝试子输入，防止不小心按到多个按键的情况
            if (current == null) {
                for (ComboType subType : type.getSubTypes()) {
                    if ((current = last.getNext(subType)) != null) {
                        break;
                    }
                }
            }
            //动画是空的就直接跳过，不是就播放
            if (current != null) {
                if (current.getAnimationAccessor() == null || !current.getConditionAnimations().isEmpty()) {
                    if (current.getConditionAnimations().isEmpty()) {
                        return;
                    }
                    current.getConditionAnimations().sort(Comparator.comparingInt(ComboNode::getPriority).reversed());
                    //多个条件指向不同动画，根据优先级来检测
                    for (ComboNode conditionAnimation : current.getConditionAnimations()) {
                        boolean canExecute = true;
                        for (Condition condition : conditionAnimation.getConditions(Side.SERVER, Side.BOTH)) {

                            if (condition instanceof PressedTimeCondition pressedTimeCondition) {
                                if (pressedTime < pressedTimeCondition.getMin() || pressedTime > pressedTimeCondition.getMax()) {
                                    canExecute = false;
                                    break;
                                }
                            } else if (condition instanceof PressIntervalCondition pressIntervalCondition) {
                                if (inputInterval < pressIntervalCondition.getMin() || inputInterval > pressIntervalCondition.getMax()) {
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
                    for (Condition condition : current.getConditions(Side.SERVER, Side.BOTH)) {
                        if (condition instanceof PressedTimeCondition pressedTimeCondition) {
                            hasPressedTimeCondition = true;
                            if (pressedTime < pressedTimeCondition.getMin() || pressedTime > pressedTimeCondition.getMax()) {
                                return;
                            }
                        } else if (condition instanceof PressIntervalCondition pressIntervalCondition) {
                            if (inputInterval < pressIntervalCondition.getMin() || inputInterval > pressIntervalCondition.getMax()) {
                                return;
                            }
                        } else if (!condition.predicate(container.getExecutor())) {
                            return;
                        }
                    }
                    if (!hasPressedTimeCondition && pressedTime > 20) {
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
                current.getOnBeginEvents().forEach(event -> {
                    event.testAndExecute(container.getExecutor(), container.getExecutor().getTarget());
                });
                initPlayer(container, invinciblePlayer, current);
                //把玩家参数以及当前节点同步给客户端
                invinciblePlayer.setCurrentNode(next);
                sendFeedback(next, container, invinciblePlayer);
                return;
            } else {
                invinciblePlayer.setCurrentNode(root);
                sendFeedback(root, container, invinciblePlayer);
            }
            if (debugMode) {
                LOGGER.debug("Bad node, return.");
            }
        });
    }

    public static void sendFeedback(ComboNode node, SkillContainer container, InvinciblePlayer invinciblePlayer) {
        if (node == null) {
            return;
        }
        SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(container.getSlotId());
        feedbackPacket.getBuffer().writeNbt(invinciblePlayer.saveNBTData(new CompoundTag()));
        EpicFightNetworkManager.sendToPlayer(feedbackPacket, container.getServerExecutor().getOriginal());
    }

    public static void executeOnServer(ServerPlayer serverPlayer, ComboType type) {
        executeOnServer(serverPlayer, type, 1, 0);
    }

    public static void executeOnServer(ServerPlayer serverPlayer, ComboType type, int pressTime, long inputInterval) {
        ServerPlayerPatch serverPlayerPatch = EpicFightCapabilities.getEntityPatch(serverPlayer, ServerPlayerPatch.class);
        if (serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack comboBasicAttack) {
            comboBasicAttack.executeOnServer(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), type, pressTime, inputInterval);
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

        if (next.getCooldown() > 0) {
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.COOLDOWN.get(), next.getCooldown());
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
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal());
        CompoundTag tag = args.readNbt();
        if (tag != null) {
            invinciblePlayer.loadNBTData(tag);
            ComboNode current = invinciblePlayer.getCurrentNode();
            if(current != null) {
                invinciblePlayer.getCurrentNode().getOnBeginEvents().forEach(event -> event.testAndExecute(container.getExecutor(), container.getExecutor().getTarget()));
            }
        }
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        //初始化连段
        resetCombo(container, container.getExecutor(), root);

        InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal()).resetPhase();
        container.getDataManager().setData(InvincibleSkillDataKeys.COOLDOWN.get(), 0);
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID, (event -> {
            ImmutableList<BiEvent> dodgeSuccessEvents = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getDodgeSuccessEvents();
            if (dodgeSuccessEvents != null) {
                dodgeSuccessEvents.forEach(dodgeEvent -> dodgeEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget()));
            }
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER.get(), Config.EFFECT_TICK.get());
        }));
        //减伤和霸体的判断
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event -> {
            InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal());
            if (event.getDamageSource() instanceof EpicFightDamageSource epicFightDamageSource && !invinciblePlayer.isCanBeInterrupt()) {
                epicFightDamageSource.setStunType(StunType.NONE);
            }
            //招架成功的判断，配合优先级-1使用
            if (event.isParried()) {
                container.getDataManager().setDataSync(InvincibleSkillDataKeys.PARRY_TIMER.get(), Config.EFFECT_TICK.get());
            }
        }));
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID, (event -> {
            ImmutableList<BiEvent> hurtEvents = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getHurtEvents();
            if (hurtEvents != null) {
                hurtEvents.forEach(hurtEvent -> hurtEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget()));
            }
            InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal());
            if (invinciblePlayer.getHurtDamageMultiplier() != 0) {
                event.attachValueModifier(ValueModifier.multiplier(invinciblePlayer.getHurtDamageMultiplier()));
            }
        }));
        //调整攻击倍率，冲击，硬直类型等
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event -> {
            InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal());
            if (invinciblePlayer.getStunTypeModifier() != null) {
                event.getDamageSource().setStunType(invinciblePlayer.getStunTypeModifier());
            }
            if (invinciblePlayer.getImpactMultiplier() != 1.0F) {
                event.getDamageSource().setBaseImpact(event.getDamageSource().getBaseImpact() * invinciblePlayer.getImpactMultiplier());
            }
            if (invinciblePlayer.getArmorNegation() != 0) {
                event.getDamageSource().setBaseArmorNegation(invinciblePlayer.getArmorNegation());
            }
            if (invinciblePlayer.getDamageMultiplier() != null) {
                event.getDamageSource().attachDamageModifier(invinciblePlayer.getDamageMultiplier());
            }
        }));
        //自己写个充能用，从物品判断防止切武器技能还在
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event -> {
            PlayerPatch<?> playerPatch = event.getPlayerPatch();
            ItemStack mainHandItem = playerPatch.getOriginal().getMainHandItem();
            CapabilityItem capabilityItem = EpicFightCapabilities.getItemStackCapability(mainHandItem);
            if (capabilityItem == null || !(capabilityItem.getInnateSkill(playerPatch, mainHandItem) instanceof ComboBasicAttack)) {
                return;
            }
            if (!InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal()).isNotCharge()) {
                if (!container.isFull()) {
                    float value = container.getResource() + event.getAttackDamage();
                    if (value > 0.0F) {
                        this.setConsumptionSynchronize(container, value);
                    }
                }
            }
            ImmutableList<BiEvent> hitEvents = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getHitSuccessEvents();
            if (hitEvents != null) {
                hitEvents.forEach(hitEvent -> hitEvent.testAndExecute(event.getPlayerPatch(), event.getTarget() == null ? event.getPlayerPatch().getTarget() : event.getTarget()));
            }
        }));
        //取消原版的普攻和跳攻
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.SKILL_CAST_EVENT, EVENT_UUID, (event -> {
            //不影响默认的普攻
            ItemStack mainHandItem = event.getPlayerPatch().getOriginal().getMainHandItem();
            if (mainHandItem.isEmpty() || !mainHandItem.getCapability(EpicFightCapabilities.CAPABILITY_ITEM).isPresent()) {
                return;
            }
            //不影响没技能但是有模板的武器
            if (EpicFightCapabilities.getItemStackCapability(mainHandItem).getInnateSkill(event.getPlayerPatch(), mainHandItem) == null) {
                return;
            }
            SkillCategory skillCategory = event.getSkillContainer().getSkill().getCategory();
            if (skillCategory.equals(SkillCategories.BASIC_ATTACK) && !event.getPlayerPatch().getOriginal().isPassenger() || skillCategory.equals(SkillCategories.AIR_ATTACK)) {
                event.setCanceled(true);
            }
        }));
        //播放walk的过渡动画
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event -> {
            Input input = event.getMovementInput();
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
        }));
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_ATTACK, EVENT_UUID);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SKILL_CAST_EVENT, EVENT_UUID);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
    }

    /**
     * 超时重置
     */
    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);
        if (!container.getExecutor().isLogicalClient() && container.getExecutor().getTickSinceLastAction() > Config.RESET_TICK.get()) {
            resetCombo(container, container.getServerExecutor(), root);
        }
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal());
        SkillDataManager manager = container.getDataManager();
        if (manager.hasData(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER.get())) {
            manager.setData(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER.get(), Math.max(manager.getDataValue(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER.get()) - 1, 0));
        }
        if (manager.hasData(InvincibleSkillDataKeys.PARRY_TIMER.get())) {
            manager.setData(InvincibleSkillDataKeys.PARRY_TIMER.get(), Math.max(manager.getDataValue(InvincibleSkillDataKeys.PARRY_TIMER.get()) - 1, 0));
        }
        if (container.getExecutor() instanceof ServerPlayerPatch serverPlayerPatch) {
            ItemStack itemStack = serverPlayerPatch.getOriginal().getMainHandItem();
            int currentCooldown = invinciblePlayer.getItemCooldown(itemStack);
            if (currentCooldown > 0) {
                currentCooldown = currentCooldown - 1;
                invinciblePlayer.setItemCooldown(itemStack, currentCooldown);
            }
            if (currentCooldown != manager.getDataValue(InvincibleSkillDataKeys.COOLDOWN.get())) {
                manager.setDataSync(InvincibleSkillDataKeys.COOLDOWN.get(), currentCooldown);
            }
        }
    }

    public void resetCombo(SkillContainer container, PlayerPatch<?> playerPatch, ComboNode root) {
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(playerPatch.getOriginal());
        invinciblePlayer.setCurrentNode(root);
        invinciblePlayer.clear();
        if(!playerPatch.isLogicalClient()) {
            sendFeedback(root, container, invinciblePlayer);
        }
    }

    @Override
    public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
        if (translationKeys.isEmpty()) {
            return super.getTooltipOnItem(itemStack, cap, playerpatch);
        }
        List<Component> list = Lists.newArrayList();
        for (String translationKey : translationKeys) {
            list.add(Component.translatable(translationKey));
        }
        return list;
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        return shouldDrawGui;
    }

    @Override
    public ResourceLocation getSkillTexture() {
        return skillTextureLocation == null ? super.getSkillTexture() : skillTextureLocation;
    }

    private static final Vec2f[] CLOCK_POS = {
            new Vec2f(0.5F, 0.5F),
            new Vec2f(0.5F, 0.0F),
            new Vec2f(0.0F, 0.0F),
            new Vec2f(0.0F, 1.0F),
            new Vec2f(1.0F, 1.0F),
            new Vec2f(1.0F, 0.0F)
    };


    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, (float)gui.getSlidingProgression(), 0);

        boolean creative = container.getExecutor().getOriginal().isCreative();
        boolean fullstack = creative || container.isFull();
        boolean canUse = !container.isDisabled() && container.getSkill().checkExecuteCondition(container);
        float cooldownRatio = (fullstack || container.isActivated()) ? 1.0F : container.getResource(partialTick);
        int vertexNum = 0;
        float iconSize = 32.0F;
        float bottom = y + iconSize;
        float right = x + iconSize;
        float middle = x + iconSize * 0.5F;
        float lastVertexX = 0;
        float lastVertexY = 0;
        float lastTexX = 0;
        float lastTexY = 0;

        if (cooldownRatio < 0.125F) {
            vertexNum = 6;
            lastTexX = cooldownRatio / 0.25F;
            lastTexY = 0.0F;
            lastVertexX = middle + iconSize * lastTexX;
            lastVertexY = y;
            lastTexX += 0.5F;
        } else if (cooldownRatio < 0.375F) {
            vertexNum = 5;
            lastTexX = 1.0F;
            lastTexY = (cooldownRatio - 0.125F) / 0.25F;
            lastVertexX = right;
            lastVertexY = y + iconSize * lastTexY;
        } else if (cooldownRatio < 0.625F) {
            vertexNum = 4;
            lastTexX = (cooldownRatio - 0.375F) / 0.25F;
            lastTexY = 1.0F;
            lastVertexX = right - iconSize * lastTexX;
            lastVertexY = bottom;
            lastTexX = 1.0F - lastTexX;
        } else if (cooldownRatio < 0.875F) {
            vertexNum = 3;
            lastTexX = 0.0F;
            lastTexY = (cooldownRatio - 0.625F) / 0.25F;
            lastVertexX = x;
            lastVertexY = bottom - iconSize * lastTexY;
            lastTexY = 1.0F - lastTexY;
        } else {
            vertexNum = 2;
            lastTexX = (cooldownRatio - 0.875F) / 0.25F;
            lastTexY = 0.0F;
            lastVertexX = x + iconSize * lastTexX;
            lastVertexY = y;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, container.getSkill().getSkillTexture());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        if (canUse) {
            if (container.getStack() > 0) {
                RenderSystem.setShaderColor(0.0F, 0.64F, 0.72F, 0.8F);
            } else {
                RenderSystem.setShaderColor(0.0F, 0.5F, 0.5F, 0.6F);
            }
        } else {
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.6F);
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);

        for (int j = 0; j < vertexNum; j++) {
            bufferbuilder.vertex(guiGraphics.pose().last().pose(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F).uv(CLOCK_POS[j].x, CLOCK_POS[j].y).endVertex();
        }

        bufferbuilder.vertex(guiGraphics.pose().last().pose(), lastVertexX, lastVertexY, 0.0F).uv(lastTexX, lastTexY).endVertex();
        tessellator.end();

        if (canUse) {
            RenderSystem.setShaderColor(0.08F, 0.79F, 0.95F, 1.0F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GL11.glCullFace(GL11.GL_FRONT);

        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);

        for (int j = 0; j < 2; j++) {
            bufferbuilder.vertex(guiGraphics.pose().last().pose(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F).uv(CLOCK_POS[j].x, CLOCK_POS[j].y).endVertex();
        }

        for (int j = CLOCK_POS.length - 1; j >= vertexNum; j--) {
            bufferbuilder.vertex(guiGraphics.pose().last().pose(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F).uv(CLOCK_POS[j].x, CLOCK_POS[j].y).endVertex();
        }

        bufferbuilder.vertex(guiGraphics.pose().last().pose(), lastVertexX, lastVertexY, 0.0F).uv(lastTexX, lastTexY).endVertex();
        tessellator.end();

        GL11.glCullFace(GL11.GL_BACK);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (container.isActivated() && (container.getSkill().getActivateType() == ActivateType.DURATION || container.getSkill().getActivateType() == ActivateType.DURATION_INFINITE)) {
            String s = String.format("%.0f", container.getRemainDuration() / 20.0F);
            int stringWidth = (gui.getFont().width(s) - 6) / 3;
            guiGraphics.drawString(gui.getFont(), s, x + 13 - stringWidth, y + 13, 16777215, true);
        } else if (!fullstack) {
            String s = String.valueOf((int)(cooldownRatio * 100.0F));
            int stringWidth = (gui.getFont().width(s) - 6) / 3;
            guiGraphics.drawString(gui.getFont(), s, x + 13 - stringWidth, y + 13, 16777215, true);
        }

        if (container.getSkill().getMaxStack() > 1) {
            String s = String.valueOf(container.getStack());
            int stringWidth = (gui.getFont().width(s) - 6) / 3;
            guiGraphics.drawString(gui.getFont(), s, x + 25 - stringWidth, y + 22, 16777215, true);
        }

        //画冷却

        SkillDataManager manager = container.getDataManager();
        if(!manager.hasData(InvincibleSkillDataKeys.COOLDOWN.get())){
            return;
        }
        int cooldown = manager.getDataValue(InvincibleSkillDataKeys.COOLDOWN.get());
        if(cooldown > 0){
            Font font = gui.getFont();
            String s = String.format("%.1fs", cooldown / 20.0);
            int stringWidth = (font.width(s) - 6) / 3;
            guiGraphics.drawString(font, s, x - stringWidth, y + 22, 16777215, true);
        }
        guiGraphics.pose().popPose();
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

    public static class Builder extends SkillBuilder<ComboBasicAttack> {
        protected ComboNode root;

        protected List<String> translationKeys = List.of();
        protected int maxPressTime, maxReserveTime, maxProtectTime;
        @Nullable
        protected AnimationManager.AnimationAccessor<? extends StaticAnimation> walkBegin, walkEnd;

        protected boolean shouldDrawGui;
        protected ResourceLocation skillTextureLocation;

        public Builder() {
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

        public Builder setSkillTextureLocation(ResourceLocation skillTextureLocation) {
            this.skillTextureLocation = skillTextureLocation;
            return this;
        }
    }

}
