package com.p1nero.invincible.api;

import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.api.combo.ComboType;
import com.p1nero.invincible.api.events.BaseEvent;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;

import java.util.function.Supplier;

public interface EventPresets {

    static BaseEvent tryExecute(ServerPlayer serverPlayer, ComboNode node) {
        return BaseEvent.createServerEvent(((playerPatch, target, invinciblePlayer) -> {
            ComboBasicAttack.executeNodeOnServer(serverPlayer, node);
        }));
    }

    static BaseEvent tryExecute(ServerPlayer serverPlayer, ComboNode node, int pressedTime, long inputInterval) {
        return BaseEvent.createServerEvent(((playerPatch, target, invinciblePlayer) -> {
            ComboBasicAttack.executeNodeOnServer(serverPlayer, node, pressedTime, inputInterval);
        }));
    }

    static BaseEvent simulateInput(ServerPlayer serverPlayer, ComboType type) {
        return BaseEvent.createServerEvent(((playerPatch, target, invinciblePlayer) -> {
            ComboBasicAttack.executeOnServer(serverPlayer, type);
        }));
    }

    static BaseEvent simulateInput(ServerPlayer serverPlayer, ComboType type, int pressedTime, long inputInterval) {
        return BaseEvent.createServerEvent(((playerPatch, target, invinciblePlayer) -> {
            ComboBasicAttack.executeOnServer(serverPlayer, type, pressedTime, inputInterval);
        }));
    }

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

    static BaseEvent addMobEffect(Supplier<Holder<MobEffect>> mobEffectSupplier, int duration, int amplifier, boolean onTarget) {
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
