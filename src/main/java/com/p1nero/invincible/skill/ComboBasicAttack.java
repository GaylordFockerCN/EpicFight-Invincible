package com.p1nero.invincible.skill;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import com.p1nero.invincible.InvincibleConfig;
import com.p1nero.invincible.InvincibleFlags;
import com.p1nero.invincible.api.Side;
import com.p1nero.invincible.conditions.PressIntervalCondition;
import com.p1nero.invincible.conditions.PressedTimeCondition;
import com.p1nero.invincible.attachment.InvincibleAttachments;
import com.p1nero.invincible.attachment.InvinciblePlayer;
import com.p1nero.invincible.client.InputManager;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.item.InvincibleItems;
import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.api.combo.ComboType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSkillFeedback;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ComboBasicAttack extends AbstractInvincibleSkill {

    public static final Logger LOGGER = LogUtils.getLogger();
    protected boolean shouldDrawGui;
    protected List<String> translationKeys;
    protected final int maxPressTime, maxReserveTime, maxProtectTime, resetTime;

    protected ComboNode root;
    @Nullable
    protected ResourceLocation skillTextureLocation;

    public ComboBasicAttack(Builder builder) {
        super(builder);
        this.shouldDrawGui = builder.shouldDrawGui;
        this.root = builder.root;
        this.translationKeys = builder.translationKeys;
        maxPressTime = builder.maxPressTime;
        maxReserveTime = builder.maxReserveTime;
        maxProtectTime = builder.maxProtectTime;
        resetTime = builder.resetTime;
        this.skillTextureLocation = builder.skillTextureLocation;
    }

    public static Builder createComboBasicAttack(Function<ComboBasicAttack.Builder, ComboBasicAttack> constructor) {
        return new Builder(constructor).setCategory(SkillCategories.WEAPON_INNATE).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.NONE);
    }

    public static ComboNode getCurrentNode(SkillContainer container) {
        return InvincibleAttachments.getPlayer(container.getExecutor().getOriginal()).getCurrentNode();
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
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        if(!args.contains(InvincibleFlags.TYPE_ID)) {
            return;
        }
        ComboType type = ComboType.ENUM_MANAGER.get(args.getInt(InvincibleFlags.TYPE_ID));
        if(args.getBoolean(InvincibleFlags.ON_PRESS)) {
            onPress(container, container.getServerExecutor(), type);
        } else {
            this.executeOnServer(container, type, args.getInt(InvincibleFlags.PRESSED_TIME), args.getLong(InvincibleFlags.PRESSED_INTERVAL));
        }
    }

    /**
     * 某个按键初次按下（按下时间长达1tick时触发）
     * 你可以在这里根据ComboType判断按键并播放蓄力动画等
     * 目前仅能发送单键
     */
    public void onPress(SkillContainer container, ServerPlayerPatch serverPlayerPatch, ComboType comboType) {

    }

    public boolean isDebugMode(SkillContainer container) {
        return container.getExecutor().getOriginal().getMainHandItem().is(InvincibleItems.DEBUG.get()) || container.getExecutor().getOriginal().getMainHandItem().is(InvincibleItems.CUSTOM_COMBO_DEMO.get());
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
        if (serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack comboBasicAttack) {
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
        if (serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack comboBasicAttack) {
            comboBasicAttack.executeOnServer(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), type, pressTime, inputInterval);
        }
    }

    /**
     * 方便额外调用
     * pressedTime不为0，防止和原版的技能键冲突。
     */
    public void executeOnServer(SkillContainer container, ComboType type, int pressedTime, long inputInterval){
        if(pressedTime > getMaxProtectTime() || pressedTime == 0) {
            return;
        }
        if (isDebugMode(container)) {
            LOGGER.debug("{} {} : pressed {} ticks. Interval: {} ms.", container.getExecutor().getOriginal().getMainHandItem().getDescriptionId(), type, pressedTime, inputInterval);
        }
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        ComboNode last = invinciblePlayer.getCurrentNode();
        if(last == null){
            return;
        }
        ComboNode current = last.getNext(type);
        //如果是空的，则尝试子输入，防止不小心按到多个按键的情况
        if(current == null){
            for(ComboType subType : type.getSubTypes()){
                if((current = last.getNext(subType)) != null){
                    break;
                }
            }
        }
        executeNodeOnServer(container, current, pressedTime, inputInterval);
    }

    public void executeNodeOnServer(SkillContainer container, @Nullable ComboNode current, int pressedTime, long inputInterval) {
        ComboNode next = current;
        boolean hasPressedTimeCondition = false;
        boolean debugMode = isDebugMode(container);
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        //动画是空的就直接跳过，不是就播放
        if (current != null) {
            if (current.getAnimationAccessor() == null || !current.getConditionNodes().isEmpty()) {
                if (current.getConditionNodes().isEmpty()) {
                    return;
                }
                current.getConditionNodes().sort(Comparator.comparingInt(ComboNode::getPriority).reversed());
                //多个条件指向不同动画，根据优先级来检测
                for (ComboNode conditionAnimation : current.getConditionNodes()) {
                    boolean canExecute = true;
                    for (Condition condition : conditionAnimation.getConditions(Side.BOTH, Side.SERVER)) {
                        if(condition instanceof PressedTimeCondition pressedTimeCondition) {
                            if(pressedTime < pressedTimeCondition.getMin() || pressedTime > pressedTimeCondition.getMax()) {
                                canExecute = false;
                                break;
                            }
                        } else if(condition instanceof PressIntervalCondition pressIntervalCondition) {
                            if(inputInterval < pressIntervalCondition.getMin() || inputInterval > pressIntervalCondition.getMax()) {
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
                for (Condition condition : current.getConditions(Side.BOTH, Side.SERVER)) {
                    if(condition instanceof PressedTimeCondition pressedTimeCondition) {
                        hasPressedTimeCondition = true;
                        if(pressedTime < pressedTimeCondition.getMin() || pressedTime > pressedTimeCondition.getMax()) {
                            return;
                        }
                    } else if(condition instanceof PressIntervalCondition pressIntervalCondition) {
                        if(inputInterval < pressIntervalCondition.getMin() || inputInterval > pressIntervalCondition.getMax()) {
                            return;
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
            current.getOnBeginEvents().forEach(event -> {
                event.testAndExecute(container.getExecutor(), container.getExecutor().getTarget(), invinciblePlayer);
            });
            initPlayer(container, invinciblePlayer, current);
            //把玩家参数以及当前节点同步给客户端
            if(current.isRepeatNode()) {
                next = current.getParentNode();
            }
            invinciblePlayer.setCurrentLogicNode(next);
            sendFeedback(next, container, invinciblePlayer);
        } else {
            invinciblePlayer.setCurrentLogicNode(root);
            sendFeedback(root, container, invinciblePlayer);
        }
    }

    public static void syncNode(ComboNode node, ServerPlayer serverPlayer) {
        SkillContainer container = EpicFightCapabilities.getEntityPatch(serverPlayer, ServerPlayerPatch.class).getSkill(SkillSlots.WEAPON_INNATE);
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(serverPlayer);
        sendFeedback(node, container, invinciblePlayer);
    }

    /**
     * 同步玩家数据及当前节点到客户端
     * */
    public static void sendFeedback(ComboNode node, SkillContainer container, InvinciblePlayer invinciblePlayer) {
        if (node == null) {
            return;
        }
        SPSkillFeedback feedbackPacket = SPSkillFeedback.executed(container.getSlot());
        feedbackPacket.arguments().put(InvincibleFlags.INVINCIBLE_PLAYER_NBT, invinciblePlayer.saveNBTData(new CompoundTag()));
        EpicFightNetworkManager.sendToPlayer(feedbackPacket, container.getServerExecutor().getOriginal());
    }

    /**
     * 根据预存来初始化玩家信息
     */
    protected void initPlayer(SkillContainer container, InvinciblePlayer invinciblePlayer, ComboNode dataNode) {
        invinciblePlayer.setPhase(dataNode.getNewPhase());
        super.initPlayer(container, invinciblePlayer, dataNode);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeOnClient(SkillContainer container, CompoundTag args) {
        if (args.contains(InvincibleFlags.INVINCIBLE_PLAYER_NBT)) {
            CompoundTag tag = args.getCompound(InvincibleFlags.INVINCIBLE_PLAYER_NBT);
            InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
            invinciblePlayer.loadNBTData(tag);
            ComboNode current = invinciblePlayer.getCurrentLogicNode();
            if(current != null) {
                invinciblePlayer.getCurrentLogicNode().getOnBeginEvents().forEach(event -> event.testAndExecute(container.getExecutor(), container.getExecutor().getTarget(), invinciblePlayer));
            }
            initPlayer(container, invinciblePlayer, invinciblePlayer.getCurrentDataNode());
        }
    }

    /**
     * 取消原版的普攻和跳攻
     */
    public void onSkillExecute(SkillCastEvent event, SkillContainer container) {
        //不影响默认的普攻
        ItemStack mainHandItem = event.getPlayerPatch().getOriginal().getMainHandItem();
        Optional<CapabilityItem> optionalCapabilityItem = EpicFightCapabilities.getItemCapability(mainHandItem);
        if (optionalCapabilityItem.isEmpty() || optionalCapabilityItem.get().isEmpty()) {
            return;
        }
        //不影响没技能但是有模板的武器
        CapabilityItem capabilityItem = EpicFightCapabilities.getItemStackCapability(mainHandItem);
        if(capabilityItem.getInnateSkill(event.getPlayerPatch(), mainHandItem) == null) {
            return;
        }
        SkillCategory skillCategory = event.getSkillContainer().getSkill().getCategory();
        if (skillCategory.equals(SkillCategories.BASIC_ATTACK) && !event.getPlayerPatch().getOriginal().isPassenger()) {
            event.cancel();
        }
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        resetCombo(container);
        super.onInitiate(container, eventListener);
        eventListener.registerEvent(EpicFightEventHooks.Player.CAST_SKILL, event -> {
            onSkillExecute(event, container);
        }, this);
    }

    /**
     * 超时重置
     */
    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        SkillDataManager manager = container.getDataManager();
//        if(manager.getDataValue(InvincibleSkillDataKeys.ANY_KEY_DOWN)) {
//            container.getExecutor().resetActionTick();
//        }
        if (!container.getExecutor().isLogicalClient() && container.getExecutor().getTickSinceLastAction() > InvincibleConfig.RESET_TICK.get()) {
            resetCombo(container);
        }
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
                manager.setDataSync(InvincibleSkillDataKeys.COOLDOWN, currentCooldown);
            }
        }
    }

    public void resetCombo(SkillContainer container) {
        setCurrentNodeSync(container, root);
    }

    public void setCurrentNodeSync(SkillContainer container, ComboNode comboNode) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        invinciblePlayer.setCurrentLogicNode(comboNode);
        invinciblePlayer.clear();
        if (!container.getExecutor().isLogicalClient()) {
            sendFeedback(comboNode, container, invinciblePlayer);
        }
    }

    public static void setCurrentNodeSync(ServerPlayerPatch serverPlayerPatch, ComboNode node) {
        if(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() instanceof ComboBasicAttack comboBasicAttack) {
            comboBasicAttack.setCurrentNodeSync(serverPlayerPatch.getSkill(SkillSlots.WEAPON_INNATE), node);
        }
    }

    /**
     * 你也可以重写它来实现自己的技能描述
     */
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
        return maxPressTime == 0 ? InvincibleConfig.MAX_PRESS_TICK.get() : maxPressTime;
    }

    public int getMaxProtectTime() {
        return maxProtectTime == 0 ? InvincibleConfig.PRESS_PROTECT_TICK.get() : maxProtectTime;
    }

    public int getMaxReserveTime() {
        return maxReserveTime == 0 ? InvincibleConfig.RESERVE_TICK.get() : maxReserveTime;
    }


    @Override
    public boolean shouldDraw(SkillContainer container) {
        return shouldDrawGui;
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
        boolean creative = container.getExecutor().getOriginal().isCreative();
        boolean fullstack = creative || container.isFull();
        boolean canUse = !container.isDisabled() && container.getSkill().checkExecuteCondition(container);
        float cooldownRatio = (fullstack || container.isActivated()) ? 1.0F : container.getResource(partialTick);
        int vertexNum;
        float iconSize = 32.0F;
        float bottom = y + iconSize;
        float right = x + iconSize;
        float middle = x + iconSize * 0.5F;
        float lastVertexX;
        float lastVertexY;
        float lastTexX;
        float lastTexY;

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
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, container.getSkill().getSkillTexture());

        if (canUse) {
            if (container.getStack() > 0) {
                RenderSystem.setShaderColor(0.0F, 0.64F, 0.72F, 0.8F);
            } else {
                RenderSystem.setShaderColor(0.0F, 0.5F, 0.5F, 0.6F);
            }
        } else {
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.6F);
        }

        PoseStack poseStack = guiGraphics.pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);

        for (int j = 0; j < vertexNum; j++) {
            bufferbuilder.addVertex(poseStack.last(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F).setUv(CLOCK_POS[j].x, CLOCK_POS[j].y);
        }

        bufferbuilder.addVertex(poseStack.last(), lastVertexX, lastVertexY, 0.0F).setUv(lastTexX, lastTexY);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

        if (canUse) {
            RenderSystem.setShaderColor(0.08F, 0.79F, 0.95F, 1.0F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GL11.glCullFace(GL11.GL_FRONT);
        bufferbuilder = tessellator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);

        for (int j = 0; j < 2; j++) {
            bufferbuilder.addVertex(poseStack.last(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F).setUv(CLOCK_POS[j].x, CLOCK_POS[j].y);
        }

        for (int j = CLOCK_POS.length - 1; j >= vertexNum; j--) {
            bufferbuilder.addVertex(poseStack.last(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F).setUv(CLOCK_POS[j].x, CLOCK_POS[j].y);
        }

        bufferbuilder.addVertex(poseStack.last(), lastVertexX, lastVertexY, 0.0F).setUv(lastTexX, lastTexY);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

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

        RenderSystem.disableBlend();

        SkillDataManager manager = container.getDataManager();
        if(!manager.hasData(InvincibleSkillDataKeys.COOLDOWN)){
            return;
        }
        int cooldown = manager.getDataValue(InvincibleSkillDataKeys.COOLDOWN);
        if(cooldown > 0){
            Vec2i pos = ClientConfig.getWeaponInnatePosition();
            String s = String.format("%.1fs", cooldown / 20.0);
            int stringWidth = (gui.getFont().width(s) - 6) / 3;
            guiGraphics.drawString(gui.getFont(), s, pos.x - stringWidth, pos.y + 22, 16777215, true);
        }
    }

    @Override
    public ResourceLocation getSkillTexture() {
        return skillTextureLocation != null ? skillTextureLocation : super.getSkillTexture();
    }

    public static class Builder extends SkillBuilder<Builder> {
        protected ComboNode root;

        protected List<String> translationKeys = List.of();

        protected boolean shouldDrawGui;
        protected int maxPressTime, maxReserveTime, maxProtectTime, resetTime;
        protected ResourceLocation skillTextureLocation;

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

        public Builder setResetTime(int resetTime) {
            this.resetTime = resetTime;
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
