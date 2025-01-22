package com.p1nero.invincible.skill;

import com.p1nero.invincible.Config;
import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.capability.InvinciblePlayer;
import com.p1nero.invincible.client.keymappings.InvincibleKeyMappings;
import com.p1nero.invincible.item.InvincibleItems;
import com.p1nero.invincible.skill.api.ComboNode;
import com.p1nero.invincible.skill.api.ComboType;
import net.minecraft.client.player.Input;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.StaticAnimationProvider;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPExecuteSkill;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

public class ComboBasicAttack extends Skill {

    private static final UUID EVENT_UUID = UUID.fromString("d1d114cc-f11f-11ed-a05b-0242ac114514");

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
     * 根据输入告诉服务端放不同的技能
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object getExecutionPacket(LocalPlayerPatch executor, FriendlyByteBuf args) {
        CPExecuteSkill packet = new CPExecuteSkill(executor.getSkill(this).getSlotId());
        packet.getBuffer().writeBoolean(InvincibleKeyMappings.KEY1.isDown());
        packet.getBuffer().writeBoolean(InvincibleKeyMappings.KEY2.isDown());
        packet.getBuffer().writeBoolean(InvincibleKeyMappings.KEY3.isDown());
        packet.getBuffer().writeBoolean(InvincibleKeyMappings.KEY4.isDown());
        return packet;
    }

    @Override
    public void executeOnServer(ServerPlayerPatch executor, FriendlyByteBuf args) {
        boolean key1 = args.readBoolean();
        boolean key2 = args.readBoolean();
        boolean key3 = args.readBoolean();
        boolean key4 = args.readBoolean();
        ComboType type = null;
        if (key1 && !key2 && !key3 && !key4) {
            type = ComboNode.ComboTypes.KEY_1;
        }
        if (!key1 && key2 && !key3 && !key4) {
            type = ComboNode.ComboTypes.KEY_2;
        }
        if (!key1 && !key2 && key3 && !key4) {
            type = ComboNode.ComboTypes.KEY_3;
        }
        if (!key1 && !key2 && !key3 && key4) {
            type = ComboNode.ComboTypes.KEY_4;
        }
        if (key1 && key2 && !key3 && !key4) {
            type = ComboNode.ComboTypes.KEY_1_2;
        }
        if (key1 && !key2 && key3 && !key4) {
            type = ComboNode.ComboTypes.KEY_1_3;
        }
        if (key1 && !key2 && !key3 && key4) {
            type = ComboNode.ComboTypes.KEY_1_4;
        }
        if (!key1 && key2 && key3 && !key4) {
            type = ComboNode.ComboTypes.KEY_2_3;
        }
        if (!key1 && key2 && !key3 && key4) {
            type = ComboNode.ComboTypes.KEY_2_4;
        }
        if (!key1 && !key2 && key3 && key4) {
            type = ComboNode.ComboTypes.KEY_3_4;
        }
        ComboType finalType = type;
        if (finalType == null) {
            //这里涉及到InputEvent的一个奇怪bug，鼠标输入会读两次
            return;
        }
        if (executor.getOriginal().getMainHandItem().is(InvincibleItems.DEBUG.get()) || executor.getOriginal().getMainHandItem().is(InvincibleItems.DATAPACK_DEBUG.get()) ) {
            System.out.println(executor.getOriginal().getMainHandItem().getDescriptionId() + " " + finalType);
        }
        SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(executor.getSkill(this).getSlotId());
        executor.getOriginal().getCapability(InvincibleCapabilityProvider.INVINCIBLE_PLAYER).ifPresent(invinciblePlayer -> {
            ComboNode current = invinciblePlayer.getCurrentNode();
            //到叶子就归位
            if (!current.hasNext()) {
                invinciblePlayer.setCurrentNode(root);
                return;
            }
            ComboNode next = current.getNext(finalType);
            //动画是空的就直接跳过，不是就播放
            if (next != null && (next.getCondition() == null || next.getCondition().predicate(executor))) {
                invinciblePlayer.clearTimeEvents();
                for(TimeStampedEvent event : next.getTimeEvents()){
                    event.resetExecuted();
                    invinciblePlayer.addTimeEvent(event);
                }
                invinciblePlayer.setPlaySpeed(next.getPlaySpeed());
                invinciblePlayer.setNotCharge(next.isNotCharge());
                feedbackPacket.getBuffer().writeNbt(invinciblePlayer.saveNBTData(new CompoundTag()));
                executor.playAnimationSynchronized(next.getAnimation(), next.getConvertTime());
            }
            invinciblePlayer.setCurrentNode(next);
        });

        EpicFightNetworkManager.sendToPlayer(feedbackPacket, executor.getOriginal());
    }

    @Override
    public void executeOnClient(LocalPlayerPatch executor, FriendlyByteBuf args) {
        CompoundTag tag = args.readNbt();
        if(tag != null){
            InvincibleCapabilityProvider.get(executor.getOriginal()).loadNBTData(tag);
        }
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID, (event -> {

        }));
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID, (event -> {

        }));
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.DEALT_DAMAGE_EVENT_HURT, EVENT_UUID, (event -> {

        }));
        //自己写个充能用
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event -> {
            if (!InvincibleCapabilityProvider.get(event.getPlayerPatch().getOriginal()).isNotCharge()) {
                if (!container.isFull()) {
                    float value = container.getResource() + event.getAttackDamage();
                    if (value > 0.0F) {
                        this.setConsumptionSynchronize(event.getPlayerPatch(), value);
                    }
                }
            }
        }));
        //取消原版的普攻和跳攻
        container.getExecuter().getEventListener().addEventListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (event -> {
            SkillCategory skillCategory = event.getSkillContainer().getSkill().getCategory();
            if (skillCategory.equals(SkillCategories.BASIC_ATTACK) || skillCategory.equals(SkillCategories.AIR_ATTACK)) {
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
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.DEALT_DAMAGE_EVENT_HURT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
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
//        return container.getDataManager().getDataValue(InvincibleSkillDataKeys.SHOULD_DRAW_GUI.get());
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
