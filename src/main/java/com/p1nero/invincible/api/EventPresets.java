package com.p1nero.invincible.api;

import com.p1nero.invincible.api.events.BaseEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;

import java.util.function.Supplier;

public interface EventPresets {
    static BaseEvent consumeStamina(float consume) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            playerPatch.setStamina(playerPatch.getStamina() - consume);
        }));
    }

    static BaseEvent setStamina(float value) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            playerPatch.setStamina(value);
        }));
    }

    static BaseEvent setStack(int value) {
        return setStack(value, SkillSlots.WEAPON_INNATE);
    }

    static BaseEvent setStack(int value, SkillSlot slot) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            playerPatch.getSkill(slot).getSkill().setStackSynchronize(playerPatch.getSkill(slot), value);
        }));
    }

    static BaseEvent consumeStack(int consume) {
        return consumeStack(consume, SkillSlots.WEAPON_INNATE);
    }

    static BaseEvent consumeStack(int consume, SkillSlot slot) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            SkillContainer container = playerPatch.getSkill(slot);
            container.getSkill().setStackSynchronize(container, Math.max(0, container.getStack() - consume));
        }));
    }

    static BaseEvent addMobEffect(Supplier<MobEffect> mobEffectSupplier, int duration, int amplifier, boolean onTarget) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            LivingEntity living = onTarget ? playerPatch.getOriginal() : playerPatch.getTarget();
            playerPatch.getOriginal().addEffect(new MobEffectInstance(mobEffectSupplier.get(), duration, amplifier));
        }));
    }

    static BaseEvent setPhase(int phase) {
        return BaseEvent.create(((playerPatch, target, invinciblePlayer) -> invinciblePlayer.setPhase(phase)));
    }

    static BaseEvent resetPhase() {
        return BaseEvent.create(((playerPatch, target, invinciblePlayer) -> invinciblePlayer.resetPhase()));
    }

}
