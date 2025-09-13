package com.p1nero.invincible.skill;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.p1nero.invincible.Config;
import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.api.events.BaseEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.attachment.InvincibleAttachments;
import com.p1nero.invincible.attachment.InvinciblePlayer;
import com.p1nero.invincible.client.InputManager;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.neoevent.playerpatch.DealDamageEvent;
import yesman.epicfight.api.neoevent.playerpatch.DodgeSuccessEvent;
import yesman.epicfight.api.neoevent.playerpatch.TakeDamageEvent;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SimpleCustomInnateSkill extends AbstractInvincibleSkill {

    protected static final UUID EVENT_UUID = UUID.fromString("d1d114cc-f11f-11ed-a05b-0242ac191981");

    protected boolean shouldDrawGui;
    protected List<String> translationKeys;
    protected ResourceLocation skillTexture;
    @Nullable
    protected AnimationManager.AnimationAccessor<? extends StaticAnimation> walkBegin, walkEnd;

    protected ComboNode node;

    public SimpleCustomInnateSkill(Builder builder) {
        super(builder);
        this.shouldDrawGui = builder.shouldDrawGui;
        this.skillTexture = builder.skillTextureLocation;
        this.node = builder.root;
        this.translationKeys = builder.translationKeys;
    }

    public static Builder createCustomInnateSkill(Function<SimpleCustomInnateSkill.Builder, SimpleCustomInnateSkill> constructor) {
        return new Builder(constructor).setCategory(SkillCategories.WEAPON_INNATE).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.NONE);
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        if (container.getExecutor().isLogicalClient()) {
            return super.canExecute(container);
        } else {
            if(container.getStack() == 0 && !container.getExecutor().getOriginal().isCreative()) {
                return false;
            }
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
        AnimationManager.AnimationAccessor animationAccessor = node.getAnimationAccessor();
        if (animationAccessor == null) {
            return;
        }
        float convertTime = node.getConvertTime();
        container.getExecutor().playAnimationSynchronized(animationAccessor, convertTime);
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        node.getOnBeginEvents().forEach(event -> {
            event.testAndExecute(container.getExecutor(), container.getExecutor().getTarget(), invinciblePlayer);
        });
        initPlayer(container, InvincibleAttachments.getPlayer(container.getServerExecutor().getOriginal()), node);
        setStackSynchronize(container, container.getStack() - 1);
    }

    /**
     * 根据预存来初始化玩家信息
     */
    private void initPlayer(SkillContainer container, InvinciblePlayer invinciblePlayer, ComboNode node) {
        invinciblePlayer.resetTimeEvents();
        ImmutableList.Builder builder = ImmutableList.<TimeStampedEvent>builder();
        for (TimeStampedEvent event : node.getTimeEvents()) {
            event.resetExecuted();
            builder.add(event);
        }
        invinciblePlayer.setTimeStampedEvents(builder.build());
        invinciblePlayer.setHurtEvents(ImmutableList.copyOf(node.getHurtEvents()));
        invinciblePlayer.setHitSuccessEvents(ImmutableList.copyOf(node.getHitEvents()));
        invinciblePlayer.setDodgeSuccessEvents(ImmutableList.copyOf(node.getDodgeSuccessEvents()));
        invinciblePlayer.setCanBeInterrupt(node.isCanBeInterrupt());
        invinciblePlayer.setPlaySpeedMultiplier(node.getPlaySpeed());
        invinciblePlayer.setNotCharge(node.isNotCharge());

        if (node.getCooldown() > 0) {
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.COOLDOWN, node.getCooldown());
            invinciblePlayer.setItemCooldown(container.getExecutor().getOriginal().getMainHandItem(), node.getCooldown());
        }

        invinciblePlayer.setArmorNegation(node.getArmorNegation());
        invinciblePlayer.setHurtDamageMultiplier(node.getHurtDamageMultiplier());
        invinciblePlayer.setDamageMultiplier(node.getDamageMultiplier());
        invinciblePlayer.setImpactMultiplier(node.getImpactMultiplier());
        invinciblePlayer.setStunTypeModifier(node.getStunTypeModifier());
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        //初始化连段
        resetCombo(container.getExecutor(), node);

        InvincibleAttachments.getPlayer(container.getExecutor().getOriginal()).resetPhase();
        container.getDataManager().setData(InvincibleSkillDataKeys.COOLDOWN, 0);
    }


    /**
     * 闪避成功事件的处理，以及闪避条件
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onDodgeSuccess(DodgeSuccessEvent event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        ImmutableList<BaseEvent> dodgeSuccessEvents = InvincibleAttachments.getPlayer(event.getPlayerPatch().getOriginal()).getDodgeSuccessEvents();
        if(dodgeSuccessEvents != null){
            dodgeSuccessEvents.forEach(dodgeEvent -> dodgeEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget(), invinciblePlayer));
        }
        container.getDataManager().setDataSync(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER, Config.EFFECT_TICK.get());
    }

    /**
     * 减伤和霸体的判断
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onHurtEventPre(TakeDamageEvent.Pre event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(event.getPlayerPatch().getOriginal());
        if (event.getDamageSource() instanceof EpicFightDamageSource epicFightDamageSource && !invinciblePlayer.isCanBeInterrupt()) {
            epicFightDamageSource.setStunType(StunType.NONE);
        }
        if (invinciblePlayer.getHurtDamageMultiplier() != 0) {
            event.attachValueModifier(ValueModifier.multiplier(invinciblePlayer.getHurtDamageMultiplier()));
        }
    }

    /**
     * 招架成功的判断
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onHurtEventIncome(TakeDamageEvent.Income event, SkillContainer container) {
        if(event.isParried()){
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.PARRY_TIMER, Config.EFFECT_TICK.get());
        }
    }

    /**
     * 抛出受伤事件
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onHurtEventPost(TakeDamageEvent.Post event, SkillContainer container) {
        ImmutableList<BaseEvent> hurtEvents = InvincibleAttachments.getPlayer(event.getPlayerPatch().getOriginal()).getHurtEvents();
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        if(hurtEvents != null){
            hurtEvents.forEach(hurtEvent -> hurtEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget(), invinciblePlayer));
        }
    }

    /**
     * 调整攻击倍率，冲击，硬直类型等
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onDealDamageEventPre(DealDamageEvent.Pre event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(event.getPlayerPatch().getOriginal());
        if (invinciblePlayer.getStunTypeModifier() != null) {
            event.getDamageSource().setStunType(invinciblePlayer.getStunTypeModifier());
        }
        if (invinciblePlayer.getImpactMultiplier() != 1.0F) {
            event.getDamageSource().setBaseImpact(event.getDamageSource().getBaseImpact() * invinciblePlayer.getImpactMultiplier());
        }
        if(invinciblePlayer.getArmorNegation() != 0){
            event.getDamageSource().setBaseArmorNegation(invinciblePlayer.getArmorNegation());
        }
        if (invinciblePlayer.getDamageMultiplier() != null) {
            event.getDamageSource().attachDamageModifier(invinciblePlayer.getDamageMultiplier());
        }
    }

    /**
     * 自己的充能
     */
    @SkillEvent(side = SkillEvent.Side.SERVER)
    public void onDealDamageEventPost(DealDamageEvent.Post event, SkillContainer container) {
        if (!InvincibleAttachments.getPlayer(event.getPlayerPatch().getOriginal()).isNotCharge()) {
            PlayerPatch<?> playerPatch = event.getPlayerPatch();
            ItemStack mainHandItem = playerPatch.getOriginal().getMainHandItem();
            CapabilityItem capabilityItem = EpicFightCapabilities.getItemStackCapability(mainHandItem);
            if(capabilityItem == null || !(capabilityItem.getInnateSkill(playerPatch, mainHandItem) instanceof ComboBasicAttack)) {
                return;
            }
            if (!container.isFull()) {
                float value = container.getResource() + event.getNeoForgeEvent().getNewDamage();
                if (value > 0.0F) {
                    this.setConsumptionSynchronize(container, value);
                }
            }
        }
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        ImmutableList<BaseEvent> hitEvents = InvincibleAttachments.getPlayer(event.getPlayerPatch().getOriginal()).getHitSuccessEvents();
        if(hitEvents != null){
            hitEvents.forEach(hitEvent -> hitEvent.testAndExecute(event.getPlayerPatch(), event.getTarget() == null ? event.getPlayerPatch().getTarget() : event.getTarget(), invinciblePlayer));
        }
    }

    /**
     * 超时重置
     */
    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);
        if (!container.getExecutor().isLogicalClient() && container.getExecutor().getTickSinceLastAction() > Config.RESET_TICK.get()) {
            resetCombo(container.getServerExecutor(), node);
        }
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        SkillDataManager manager = container.getDataManager();
        if (manager.hasData(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER)) {
            manager.setData(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER, Math.max(manager.getDataValue(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER) - 1, 0));
        }
        if (manager.hasData(InvincibleSkillDataKeys.PARRY_TIMER)) {
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

    public void resetCombo(PlayerPatch<?> playerPatch, ComboNode root) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(playerPatch.getOriginal());
        invinciblePlayer.setCurrentNode(root);
        invinciblePlayer.clear();
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
        return skillTexture == null ? super.getSkillTexture() : skillTexture;
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

    public static class Builder extends SkillBuilder<Builder> {
        protected ComboNode root;

        protected List<String> translationKeys = List.of();

        protected boolean shouldDrawGui;
        protected ResourceLocation skillTextureLocation;

        public Builder(Function<SimpleCustomInnateSkill.Builder, ? extends SimpleCustomInnateSkill> constructor) {
            super(constructor);
        }

        public Builder setCategory(SkillCategory category) {
            this.category = category;
            return this;
        }

        public Builder setActivateType(ActivateType activateType) {
            this.activateType = activateType;
            return this;
        }

        public Builder setResource(Resource resource) {
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
