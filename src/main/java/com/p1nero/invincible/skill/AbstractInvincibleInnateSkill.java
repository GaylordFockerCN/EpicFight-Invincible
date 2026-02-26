package com.p1nero.invincible.skill;

import com.p1nero.invincible.InvincibleConfig;
import com.p1nero.invincible.api.events.BaseEvent;
import com.p1nero.invincible.api.events.HitEvent;
import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.capability.InvinciblePlayer;
import com.p1nero.invincible.capability.InvinciblePlayerCapabilityProvider;
import com.p1nero.invincible.damagesource.InvincibleDamageTypeTags;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.eventlistener.DealDamageEvent;
import yesman.epicfight.world.entity.eventlistener.DodgeSuccessEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

import java.util.List;
import java.util.UUID;

public class AbstractInvincibleInnateSkill extends Skill {

    protected static final UUID EVENT_UUID = UUID.fromString("d1d114cc-f11f-11ed-a05b-0242ac114514");

    public AbstractInvincibleInnateSkill(SkillBuilder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal()).resetPhase();
        container.getDataManager().setData(InvincibleSkillDataKeys.COOLDOWN.get(), 0);
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID, (event -> {
            onDodgeSuccess(event, container);
        }));
        //减伤和霸体的判断
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event -> {
            onTakeDamageEventAttack(event, container);
        }));
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID, (event -> {
            onTakeDamageEventHurt(event, container);
        }));
        //调整攻击倍率，冲击，硬直类型等
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event -> {
            onDealDamageEventAttack(event, container);
        }));
        //自己写个充能用，从物品判断防止切武器技能还在
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event -> {
            onDealDamageEventDamage(event, container);
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
    }

    protected void handleStiff(SkillContainer container, AnimationManager.AnimationAccessor animationAccessor) {
        boolean stiffAttack = EpicFightGameRules.STIFF_COMBO_ATTACKS.getRuleValue(container.getExecutor().getOriginal().level());
        SPAnimatorControl animatorControlPacket;
        if (stiffAttack) {
            animatorControlPacket = new SPAnimatorControl(AnimatorControlPacket.Action.PLAY, animationAccessor, 0.0F, container.getExecutor());
        } else {
            animatorControlPacket = new SPAnimatorControl(AnimatorControlPacket.Action.PLAY_CLIENT, animationAccessor, 0.0F, container.getExecutor(), AnimatorControlPacket.Layer.COMPOSITE_LAYER, AnimatorControlPacket.Priority.HIGHEST);
        }
        EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(animatorControlPacket, container.getServerExecutor().getOriginal());

    }

    /**
     * 根据预存来初始化玩家信息
     */
    protected void initPlayer(SkillContainer container, InvinciblePlayer invinciblePlayer, ComboNode dataNode) {
        invinciblePlayer.setCurrentDataNode(dataNode);
        if (dataNode.getCooldown() > 0) {
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.COOLDOWN.get(), dataNode.getCooldown());
            invinciblePlayer.setItemCooldown(container.getExecutor().getOriginal().getMainHandItem(), dataNode.getCooldown());
        }
    }


    protected void onDodgeSuccess(DodgeSuccessEvent event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal());
        List<BaseEvent> dodgeSuccessEvents = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getDodgeSuccessEvents();
        if (dodgeSuccessEvents != null) {
            dodgeSuccessEvents.forEach(dodgeEvent -> dodgeEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget(), invinciblePlayer));
        }
        container.getDataManager().setDataSync(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER.get(), InvincibleConfig.EFFECT_TICK.get());
    }

    protected void onTakeDamageEventAttack(TakeDamageEvent.Attack event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal());
        if (event.getDamageSource() instanceof EpicFightDamageSource epicFightDamageSource && !invinciblePlayer.canBeInterrupt()) {
            epicFightDamageSource.setStunType(StunType.NONE);
        }
        //招架成功的判断，配合优先级-1使用
        if (event.isParried()) {
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.PARRY_TIMER.get(), InvincibleConfig.EFFECT_TICK.get());
        }
    }

    protected void onTakeDamageEventHurt(TakeDamageEvent.Hurt event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal());
        List<BaseEvent> hurtEvents = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getHurtEvents();
        if (hurtEvents != null) {
            hurtEvents.forEach(hurtEvent -> hurtEvent.testAndExecute(event.getPlayerPatch(), event.getPlayerPatch().getTarget(), invinciblePlayer));
        }
        if (invinciblePlayer.getHurtDamageMultiplier() != 0) {
            event.attachValueModifier(ValueModifier.multiplier(invinciblePlayer.getHurtDamageMultiplier()));
        }
    }

    protected void onDealDamageEventAttack(DealDamageEvent.Attack event, SkillContainer container) {
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
    }


    /**
     * 判断是否允许进行充能
     */
    protected boolean shouldCharge(DealDamageEvent.Damage event, SkillContainer container, InvinciblePlayer invinciblePlayer) {
        return !event.getDamageSource().is(InvincibleDamageTypeTags.NOT_CHARGE) && !invinciblePlayer.isNotCharge();
    }

    protected void onDealDamageEventDamage(DealDamageEvent.Damage event, SkillContainer container) {
        PlayerPatch<?> playerPatch = event.getPlayerPatch();
        ItemStack mainHandItem = playerPatch.getOriginal().getMainHandItem();
        CapabilityItem capabilityItem = EpicFightCapabilities.getItemStackCapability(mainHandItem);
        if (capabilityItem == null || !(capabilityItem.getInnateSkill(playerPatch, mainHandItem) instanceof ComboBasicAttack)) {
            return;
        }
        InvinciblePlayer invinciblePlayer = InvinciblePlayerCapabilityProvider.get(container.getExecutor().getOriginal());
        if (shouldCharge(event, container, invinciblePlayer)) {
            if (!container.isFull()) {
                float value = container.getResource() + event.getAttackDamage();
                if (value > 0.0F) {
                    this.setConsumptionSynchronize(container, value);
                }
            }
        }
        AnimationPlayer animationPlayer = playerPatch.getAnimator().getPlayerFor(null);
        if(animationPlayer != null && event.getDamageSource().getAnimation().equals(animationPlayer.getAnimation())) {
            List<BaseEvent> hitEvents = InvinciblePlayerCapabilityProvider.get(event.getPlayerPatch().getOriginal()).getHitSuccessEvents();
            if (hitEvents != null) {
                Entity target = event.getTarget() == null ? event.getPlayerPatch().getTarget() : event.getTarget();
                hitEvents.forEach(baseEvent -> {
                    if (baseEvent instanceof HitEvent hitEvent && animationPlayer.getRealAnimation().get() instanceof AttackAnimation attackAnimation) {
                        if (hitEvent.phaseIndex >= 0 && attackAnimation.getPhaseOrderByTime(animationPlayer.getElapsedTime()) != hitEvent.phaseIndex) {
                            return;
                        }
                    }
                    baseEvent.testAndExecute(event.getPlayerPatch(), target, invinciblePlayer);
                });
            }
        }
    }

}
