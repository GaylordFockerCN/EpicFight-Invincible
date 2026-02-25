package com.p1nero.invincible.skill;

import com.p1nero.invincible.InvincibleConfig;
import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.api.events.BaseEvent;
import com.p1nero.invincible.api.events.HitEvent;
import com.p1nero.invincible.attachment.InvincibleAttachments;
import com.p1nero.invincible.attachment.InvinciblePlayer;
import com.p1nero.invincible.damagesource.InvincibleDamageTypeTags;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.entity.DealDamageEvent;
import yesman.epicfight.api.event.types.entity.DodgeEvent;
import yesman.epicfight.api.event.types.entity.TakeDamageEvent;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

import java.util.List;

public class AbstractInvincibleSkill extends Skill {

    public AbstractInvincibleSkill(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        InvincibleAttachments.getPlayer(container.getExecutor().getOriginal()).resetPhase();
        container.getDataManager().setData(InvincibleSkillDataKeys.COOLDOWN, 0);

        eventListener.registerEvent(EpicFightEventHooks.Entity.ON_DODGE, event -> {
            onDodgeSuccess(event, container);
        }, this);
        eventListener.registerEvent(EpicFightEventHooks.Entity.TAKE_DAMAGE_PRE, event -> {
            onHurtEventPre(event, container);
        }, this);
        eventListener.registerEvent(EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME, event -> {
            onHurtEventIncome(event, container);
        }, this);
        eventListener.registerEvent(EpicFightEventHooks.Entity.TAKE_DAMAGE_POST, event -> {
            onHurtEventPost(event, container);
        }, this);
        eventListener.registerEvent(EpicFightEventHooks.Entity.DELIVER_DAMAGE_PRE, event -> {
            onDealDamageEventPre(event, container);
        }, this);
        eventListener.registerEvent(EpicFightEventHooks.Entity.DELIVER_DAMAGE_POST, event -> {
            onDealDamageEventPost(event, container);
        }, this);
    }

    /**
     * 根据预存来初始化玩家信息
     */
    protected void initPlayer(SkillContainer container, InvinciblePlayer invinciblePlayer, ComboNode dataNode) {
        invinciblePlayer.setCurrentDataNode(dataNode);
        if (dataNode.getCooldown() > 0 && !container.getExecutor().isLogicalClient()) {
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.COOLDOWN, dataNode.getCooldown());
            invinciblePlayer.setItemCooldown(container.getExecutor().getOriginal().getMainHandItem(), dataNode.getCooldown());
        }
    }

    /**
     * 闪避成功事件的处理，以及闪避条件
     */
    public void onDodgeSuccess(DodgeEvent event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        List<BaseEvent> dodgeSuccessEvents = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal()).getDodgeSuccessEvents();
        if(dodgeSuccessEvents != null){
            dodgeSuccessEvents.forEach(dodgeEvent -> dodgeEvent.testAndExecute(container.getExecutor(), container.getExecutor().getTarget(), invinciblePlayer));
        }
        container.getDataManager().setDataSync(InvincibleSkillDataKeys.DODGE_SUCCESS_TIMER, InvincibleConfig.EFFECT_TICK.get());
    }

    /**
     * 减伤和霸体的判断
     */
    public void onHurtEventPre(TakeDamageEvent.Pre event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        if (event.getDamageSource() instanceof EpicFightDamageSource epicFightDamageSource && !invinciblePlayer.canBeInterrupt()) {
            epicFightDamageSource.setStunType(StunType.NONE);
        }
        if (invinciblePlayer.getHurtDamageMultiplier() != 0) {
            event.attachValueModifier(ValueModifier.multiplier(invinciblePlayer.getHurtDamageMultiplier()));
        }
    }

    /**
     * 招架成功的判断
     */
    public void onHurtEventIncome(TakeDamageEvent.Income event, SkillContainer container) {
        if(event.isParried()){
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.PARRY_TIMER, InvincibleConfig.EFFECT_TICK.get());
        }
    }

    /**
     * 调整攻击倍率，冲击，硬直类型等
     */
    public void onDealDamageEventPre(DealDamageEvent.Pre event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
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
     * 抛出受伤事件
     */
    public void onHurtEventPost(TakeDamageEvent.Post event, SkillContainer container) {
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal());
        List<BaseEvent> hurtEvents = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal()).getHurtEvents();
        if(hurtEvents != null){
            hurtEvents.forEach(hurtEvent -> hurtEvent.testAndExecute(container.getExecutor(), container.getExecutor().getTarget(), invinciblePlayer));
        }
    }

    /**
     * 判断是否允许进行充能
     */
    protected boolean shouldCharge(DealDamageEvent.Post event, SkillContainer container, InvinciblePlayer invinciblePlayer) {
        return !event.getDamageSource().is(InvincibleDamageTypeTags.NOT_CHARGE) && !invinciblePlayer.isNotCharge();
    }

    /**
     * 自己的充能
     */
    public void onDealDamageEventPost(DealDamageEvent.Post event, SkillContainer container) {
        PlayerPatch<?> playerPatch = container.getExecutor();
        InvinciblePlayer invinciblePlayer = InvincibleAttachments.getPlayer(playerPatch);
        if (shouldCharge(event, container, invinciblePlayer)) {
            ItemStack mainHandItem = playerPatch.getOriginal().getMainHandItem();
            CapabilityItem capabilityItem = EpicFightCapabilities.getItemStackCapability(mainHandItem);
            if(!(capabilityItem.getInnateSkill(playerPatch, mainHandItem) instanceof ComboBasicAttack)) {
                return;
            }
            if (!container.isFull()) {
                float value = container.getResource() + event.getModifiedDamage();
                if (value > 0.0F) {
                    this.setConsumptionSynchronize(container, value);
                }
            }
        }
        AnimationPlayer animationPlayer = playerPatch.getAnimator().getPlayerFor(null);
        if(animationPlayer != null && event.getDamageSource().getAnimation().equals(animationPlayer.getAnimation())) {
            List<BaseEvent> hitEvents = InvincibleAttachments.getPlayer(container.getExecutor().getOriginal()).getHitSuccessEvents();
            if(hitEvents != null){
                Entity target = event.getTarget();
                hitEvents.forEach(baseEvent -> {
                    if(baseEvent instanceof HitEvent hitEvent && animationPlayer.getRealAnimation().get() instanceof AttackAnimation attackAnimation) {
                        if(hitEvent.phaseIndex >= 0 && attackAnimation.getPhaseOrderByTime(animationPlayer.getElapsedTime()) != hitEvent.phaseIndex) {
                            return;
                        }
                    }
                    baseEvent.testAndExecute(container.getExecutor(), target, invinciblePlayer);
                });
            }
        }
    }

}
