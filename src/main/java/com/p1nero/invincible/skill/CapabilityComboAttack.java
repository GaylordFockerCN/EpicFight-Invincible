package com.p1nero.invincible.skill;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.p1nero.invincible.InvincibleConfig;
import com.p1nero.invincible.api.events.Side;
import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.api.skill.ComboType;
import com.p1nero.invincible.capability.InvinciblePlayer;
import com.p1nero.invincible.capability.InvinciblePlayerCapabilityProvider;
import com.p1nero.invincible.capability.item.InvincibleWeaponCapability;
import com.p1nero.invincible.conditions.PressIntervalCondition;
import com.p1nero.invincible.conditions.PressedTimeCondition;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.item.InvincibleItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.MovementInputEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * TODO 额外做个stack系统
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CapabilityComboAttack extends AbstractInvincibleInnateSkill {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Vec2f[] CLOCK_POS = {
            new Vec2f(0.5F, 0.5F),
            new Vec2f(0.5F, 0.0F),
            new Vec2f(0.0F, 0.0F),
            new Vec2f(0.0F, 1.0F),
            new Vec2f(1.0F, 1.0F),
            new Vec2f(1.0F, 0.0F)
    };

    public CapabilityComboAttack(Builder builder) {
        super(builder);
    }

    public static Builder createCapabilityComboAttack() {
        return new Builder().setCategory(SkillCategories.WEAPON_INNATE).setActivateType(Skill.ActivateType.ONE_SHOT).setResource(Resource.NONE);
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        if (container.getExecutor().isLogicalClient()) {
            return super.canExecute(container);
        } else {
            ItemStack itemstack = container.getExecutor().getOriginal().getMainHandItem();
            return super.canExecute(container)
                    && EpicFightCapabilities.getItemStackCapability(itemstack).getInnateSkill(container.getExecutor(), itemstack) == this
                    && container.getExecutor().getOriginal().getVehicle() == null;
        }
    }

    @Override
    public boolean isExecutableState(PlayerPatch<?> executor) {
        return executor.getEntityState().canBasicAttack() && !executor.getOriginal().isSpectator();
    }

    public boolean isDebugMode(SkillContainer container) {
        return container.getExecutor().getOriginal().getMainHandItem().is(InvincibleItems.DEBUG.get())
                || container.getExecutor().getOriginal().getMainHandItem().is(InvincibleItems.CUSTOM_COMBO_DEMO.get());
    }

    @Nullable
    private static InvincibleWeaponCapability getInvincibleWeaponCapability(PlayerPatch<?> playerPatch, ItemStack itemStack) {
        CapabilityItem capabilityItem = EpicFightCapabilities.getItemStackCapability(itemStack);
        return capabilityItem instanceof InvincibleWeaponCapability invincibleWeaponCapability ? invincibleWeaponCapability : null;
    }

    @Nullable
    private InvincibleWeaponCapability getCurrentWeaponCapability(PlayerPatch<?> playerPatch) {
        return getInvincibleWeaponCapability(playerPatch, playerPatch.getOriginal().getMainHandItem());
    }

    @Nullable
    private ComboNode getCurrentComboRoot(PlayerPatch<?> playerPatch) {
        InvincibleWeaponCapability capability = getCurrentWeaponCapability(playerPatch);
        return capability == null ? null : capability.getCombo(capability.getStyle(playerPatch));
    }

    private int getMaxPressTime(@Nullable InvincibleWeaponCapability capability) {
        return capability == null || capability.getMaxPressTime() == 0 ? InvincibleConfig.MAX_PRESS_TICK.get() : capability.getMaxPressTime();
    }

    private int getMaxProtectTime(@Nullable InvincibleWeaponCapability capability) {
        return capability == null || capability.getMaxProtectTime() == 0 ? InvincibleConfig.PRESS_PROTECT_TICK.get() : capability.getMaxProtectTime();
    }

    private int getMaxReserveTime(@Nullable InvincibleWeaponCapability capability) {
        return capability == null || capability.getMaxReserveTime() == 0 ? InvincibleConfig.RESERVE_TICK.get() : capability.getMaxReserveTime();
    }

    private int getResetTime(@Nullable InvincibleWeaponCapability capability) {
        return capability == null || capability.getResetTime() == 0 ? InvincibleConfig.RESET_TICK.get() : capability.getResetTime();
    }

    public int getMaxPressTime(SkillContainer container) {
        return getMaxPressTime(getCurrentWeaponCapability(container.getExecutor()));
    }

    public int getMaxProtectTime(SkillContainer container) {
        return getMaxProtectTime(getCurrentWeaponCapability(container.getExecutor()));
    }

    public int getMaxReserveTime(SkillContainer container) {
        return getMaxReserveTime(getCurrentWeaponCapability(container.getExecutor()));
    }

    public int getResetTime(SkillContainer container) {
        return getResetTime(getCurrentWeaponCapability(container.getExecutor()));
    }

    private ResourceLocation getSkillTexture(SkillContainer container) {
        InvincibleWeaponCapability capability = getCurrentWeaponCapability(container.getExecutor());
        return capability == null || capability.getSkillTextureLocation() == null ? super.getSkillTexture() : capability.getSkillTextureLocation();
    }

    public static void executeNodeOnServer(ServerPlayer serverPlayer, ComboNode node) {
        executeNodeOnServer(serverPlayer, node, 1, 0);
    }

    public static void executeNodeOnServer(ServerPlayer serverPlayer, ComboNode node, int pressTime, long inputInterval) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(serverPlayer, ServerPlayerPatch.class).ifPresent(serverPlayerPatch -> {
            executeNodeOnServer(serverPlayerPatch, node, pressTime, inputInterval);
        });
    }

    public static void executeNodeOnServer(ServerPlayerPatch serverPlayerPatch, ComboNode node) {
        executeNodeOnServer(serverPlayerPatch, node, 1, 0);
    }

    public static void executeNodeOnServer(ServerPlayerPatch serverPlayerPatch, ComboNode node, int pressTime, long inputInterval) {
        Skill skill = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill();
        if (skill instanceof CapabilityComboAttack capabilityComboAttack) {
            capabilityComboAttack.executeNodeOnServer(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), node, pressTime, inputInterval);
        } else if (skill instanceof ComboBasicAttack comboBasicAttack) {
            comboBasicAttack.executeNodeOnServer(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), node, pressTime, inputInterval);
        }
    }

    public static void executeOnServer(ServerPlayer serverPlayer, ComboType type) {
        executeOnServer(serverPlayer, type, 1, 0);
    }

    public static void executeOnServer(ServerPlayer serverPlayer, ComboType type, int pressTime, long inputInterval) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(serverPlayer, ServerPlayerPatch.class).ifPresent(serverPlayerPatch -> {
            executeOnServer(serverPlayerPatch, type, pressTime, inputInterval);
        });
    }

    public static void executeOnServer(ServerPlayerPatch serverPlayerPatch, ComboType type) {
        executeOnServer(serverPlayerPatch, type, 1, 0);
    }

    public static void executeOnServer(ServerPlayerPatch serverPlayerPatch, ComboType type, int pressTime, long inputInterval) {
        Skill skill = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill();
        if (skill instanceof CapabilityComboAttack capabilityComboAttack) {
            capabilityComboAttack.executeOnServer(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), type, pressTime, inputInterval);
        } else if (skill instanceof ComboBasicAttack comboBasicAttack) {
            comboBasicAttack.executeOnServer(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), type, pressTime, inputInterval);
        }
    }

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

    public void executeOnServer(SkillContainer container, ComboType type, int pressedTime, long inputInterval) {
        if (pressedTime > getMaxProtectTime(container)) {
            return;
        }
        if (isDebugMode(container)) {
            LOGGER.debug("{} {} : pressed {} ticks. Interval: {} ms.", container.getExecutor().getOriginal().getMainHandItem().getDescriptionId(), type, pressedTime, inputInterval);
        }
        container.getExecutor().getOriginal().getCapability(InvinciblePlayerCapabilityProvider.INVINCIBLE_PLAYER).ifPresent(invinciblePlayer -> {
            ComboNode last = invinciblePlayer.getCurrentLogicNode();
            if (last == null) {
                last = getCurrentComboRoot(container.getExecutor());
                if (last == null) {
                    return;
                }
            }
            ComboNode current = last.getNext(type);
            if (current == null) {
                for (ComboType subType : type.getSubTypes()) {
                    if ((current = last.getNext(subType)) != null) {
                        break;
                    }
                }
            }
            executeNodeOnServer(container, current, pressedTime, inputInterval);
        });
    }

    public void executeNodeOnServer(SkillContainer container, @Nullable ComboNode current, int pressedTime, long inputInterval) {
        ComboNode next = current;
        boolean hasPressedTimeCondition = false;
        boolean debugMode = isDebugMode(container);
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal());
        if (current != null) {
            if (current.getAnimationAccessor() == null || !current.getConditionNodes().isEmpty()) {
                if (current.getConditionNodes().isEmpty()) {
                    return;
                }
                current.getConditionNodes().sort(Comparator.comparingInt(ComboNode::getPriority).reversed());
                for (ComboNode conditionAnimation : current.getConditionNodes()) {
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
                        if (conditionAnimation.hasNext()) {
                            next = conditionAnimation;
                        }
                        break;
                    }
                }
            } else {
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
            handleStiff(container, animationAccessor);
            current.getOnBeginEvents().forEach(event -> event.testAndExecute(container.getExecutor(), container.getExecutor().getTarget(), invinciblePlayer));
            initPlayer(container, invinciblePlayer, current);
            if (current.isRepeatNode()) {
                next = current.getParentNode();
            }
            invinciblePlayer.setCurrentLogicNode(next);
            sendFeedback(next, container, invinciblePlayer);
            return;
        }

        ComboNode root = getCurrentComboRoot(container.getExecutor());
        if (root != null) {
            invinciblePlayer.setCurrentLogicNode(root);
            sendFeedback(root, container, invinciblePlayer);
        }
        if (debugMode) {
            LOGGER.debug("Bad node, return.");
        }
    }

    public static void sendFeedback(ComboNode node, SkillContainer container, InvinciblePlayer invinciblePlayer) {
        if (node == null) {
            return;
        }
        SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(container.getSlotId());
        feedbackPacket.getBuffer().writeNbt(invinciblePlayer.saveNBTData(new CompoundTag()));
        EpicFightNetworkManager.sendToPlayer(feedbackPacket, container.getServerExecutor().getOriginal());
    }

    @Override
    protected void initPlayer(SkillContainer container, InvinciblePlayer invinciblePlayer, ComboNode dataNode) {
        invinciblePlayer.setPhase(dataNode.getNewPhase());
        super.initPlayer(container, invinciblePlayer, dataNode);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal());
        CompoundTag tag = args.readNbt();
        if (tag != null) {
            invinciblePlayer.loadNBTData(tag);
            ComboNode current = invinciblePlayer.getCurrentLogicNode();
            if (current != null) {
                current.getOnBeginEvents().forEach(event -> event.testAndExecute(container.getExecutor(), container.getExecutor().getTarget(), invinciblePlayer));
            }
            initPlayer(container, invinciblePlayer, invinciblePlayer.getCurrentDataNode());
        }
    }

    protected void onSkillCastEvent(SkillCastEvent event, SkillContainer container) {
        ItemStack mainHandItem = event.getPlayerPatch().getOriginal().getMainHandItem();
        Optional<CapabilityItem> optionalCapabilityItem = EpicFightCapabilities.getItemCapability(mainHandItem);
        if (optionalCapabilityItem.isEmpty() || optionalCapabilityItem.get().isEmpty()) {
            return;
        }
        if (EpicFightCapabilities.getItemStackCapability(mainHandItem).getInnateSkill(event.getPlayerPatch(), mainHandItem) == null) {
            return;
        }
        SkillCategory skillCategory = event.getSkillContainer().getSkill().getCategory();
        if (skillCategory.equals(SkillCategories.BASIC_ATTACK) && !event.getPlayerPatch().getOriginal().isPassenger()) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        resetCombo(container);
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.SKILL_CAST_EVENT, EVENT_UUID, event -> onSkillCastEvent(event, container));
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SKILL_CAST_EVENT, EVENT_UUID);
    }

    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);
        if (!container.getExecutor().isLogicalClient() && container.getExecutor().getTickSinceLastAction() > getResetTime(container)) {
            resetCombo(container);
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

    public void resetCombo(SkillContainer container) {
        setCurrentNodeSync(container, getCurrentComboRoot(container.getExecutor()));
    }

    public static ComboNode getCurrentNode(SkillContainer container) {
        return InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal()).getCurrentLogicNode();
    }

    public void setCurrentNodeSync(SkillContainer container, @Nullable ComboNode comboNode) {
        if (comboNode == null) {
            return;
        }
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal());
        invinciblePlayer.setCurrentLogicNode(comboNode);
        invinciblePlayer.clear();
        if (!container.getExecutor().isLogicalClient()) {
            sendFeedback(comboNode, container, invinciblePlayer);
        }
    }

    public static void setCurrentNodeSync(ServerPlayerPatch serverPlayerPatch, ComboNode node) {
        Skill skill = serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill();
        if (skill instanceof CapabilityComboAttack capabilityComboAttack) {
            capabilityComboAttack.setCurrentNodeSync(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), node);
        } else if (skill instanceof ComboBasicAttack comboBasicAttack) {
            comboBasicAttack.setCurrentNodeSync(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), node);
        }
    }

    @Override
    public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
        InvincibleWeaponCapability capability = cap instanceof InvincibleWeaponCapability invincibleWeaponCapability
                ? invincibleWeaponCapability
                : getInvincibleWeaponCapability(playerpatch, itemStack);
        if (capability == null || capability.getSkillDescriptions().isEmpty()) {
            return super.getTooltipOnItem(itemStack, cap, playerpatch);
        }
        List<Component> list = Lists.newArrayList();
        for (String translationKey : capability.getSkillDescriptions()) {
            list.add(Component.translatable(translationKey));
        }
        return list;
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        InvincibleWeaponCapability capability = getCurrentWeaponCapability(container.getExecutor());
        return capability != null && capability.shouldDrawSkillIcon();
    }

    @Override
    public ResourceLocation getSkillTexture() {
        return super.getSkillTexture();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, (float) gui.getSlidingProgression(), 0);

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
        RenderSystem.setShaderTexture(0, getSkillTexture(container));
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

        if (container.isActivated() && (container.getSkill().getActivateType() == Skill.ActivateType.DURATION || container.getSkill().getActivateType() == Skill.ActivateType.DURATION_INFINITE)) {
            String s = String.format("%.0f", container.getRemainDuration() / 20.0F);
            int stringWidth = (gui.getFont().width(s) - 6) / 3;
            guiGraphics.drawString(gui.getFont(), s, x + 13 - stringWidth, y + 13, 16777215, true);
        } else if (!fullstack) {
            String s = String.valueOf((int) (cooldownRatio * 100.0F));
            int stringWidth = (gui.getFont().width(s) - 6) / 3;
            guiGraphics.drawString(gui.getFont(), s, x + 13 - stringWidth, y + 13, 16777215, true);
        }

        if (container.getSkill().getMaxStack() > 1) {
            String s = String.valueOf(container.getStack());
            int stringWidth = (gui.getFont().width(s) - 6) / 3;
            guiGraphics.drawString(gui.getFont(), s, x + 25 - stringWidth, y + 22, 16777215, true);
        }

        SkillDataManager manager = container.getDataManager();
        if (!manager.hasData(InvincibleSkillDataKeys.COOLDOWN.get())) {
            guiGraphics.pose().popPose();
            return;
        }

        int cooldown = manager.getDataValue(InvincibleSkillDataKeys.COOLDOWN.get());
        if (cooldown > 0) {
            Font font = gui.getFont();
            String s = String.format("%.1fs", cooldown / 20.0);
            int stringWidth = (font.width(s) - 6) / 3;
            guiGraphics.drawString(font, s, x - stringWidth, y + 22, 16777215, true);
        }
        guiGraphics.pose().popPose();
    }

    public static class Builder extends SkillBuilder<CapabilityComboAttack> {
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
    }
}
