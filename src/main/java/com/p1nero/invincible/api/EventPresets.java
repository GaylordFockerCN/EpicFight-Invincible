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
    default BaseEvent consumeStamina(float consume) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            playerPatch.setStamina(playerPatch.getStamina() - consume);
        }));
    }

    default BaseEvent setStamina(float value) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            playerPatch.setStamina(value);
        }));
    }

    default BaseEvent setStack(int value) {
        return setStack(value, SkillSlots.WEAPON_INNATE);
    }

    default BaseEvent setStack(int value, SkillSlot slot) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            playerPatch.getSkill(slot).getSkill().setStackSynchronize(playerPatch.getSkill(slot), value);
        }));
    }

    default BaseEvent consumeStack(int consume) {
        return consumeStack(consume, SkillSlots.WEAPON_INNATE);
    }

    default BaseEvent consumeStack(int consume, SkillSlot slot) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            SkillContainer container = playerPatch.getSkill(slot);
            container.getSkill().setStackSynchronize(container, Math.max(0, container.getStack() - consume));
        }));
    }

    default BaseEvent addMobEffect(Supplier<MobEffect> mobEffectSupplier, int duration, int amplifier, boolean onTarget) {
        return BaseEvent.createServerEvent(((playerPatch, entity, invinciblePlayer) -> {
            LivingEntity living = onTarget ? playerPatch.getOriginal() : playerPatch.getTarget();
            playerPatch.getOriginal().addEffect(new MobEffectInstance(mobEffectSupplier.get(), duration, amplifier));
        }));
    }

    default BaseEvent setPhase(int phase) {
        return BaseEvent.create(((playerPatch, target, invinciblePlayer) -> invinciblePlayer.setPhase(phase)));
    }

    default BaseEvent resetPhase() {
        return BaseEvent.create(((playerPatch, target, invinciblePlayer) -> invinciblePlayer.resetPhase()));
    }

}
