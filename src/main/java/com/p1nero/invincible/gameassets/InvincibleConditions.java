package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.conditions.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.registry.EpicFightRegistries;

import java.util.function.Supplier;

public class InvincibleConditions {
    public static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS = DeferredRegister.create(EpicFightRegistries.CONDITION, InvincibleMod.MOD_ID);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> JUMP_CONDITION = CONDITIONS.register("jumping", () -> JumpCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> DASH_CONDITION = CONDITIONS.register("sprinting", () -> SprintingCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> STACK_CONDITION = CONDITIONS.register("stack_count", () -> StackCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> PHASE = CONDITIONS.register("phase", () -> PlayerPhaseCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> COOLDOWN = CONDITIONS.register("cooldown", () -> CooldownCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> MOB_EFFECT = CONDITIONS.register("mob_effect", () -> MobEffectCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> ENCHANTMENT = CONDITIONS.register("enchantment", () -> EnchantmentCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> BLOCKING = CONDITIONS.register("blocking", () -> BlockingCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> TARGET_BLOCKING = CONDITIONS.register("target_blocking", () -> TargetBlockingCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> HAS_VEHICLE = CONDITIONS.register("has_vehicle", () -> VehicleCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> DODGE_SUCCESS = CONDITIONS.register("dodge_success", () -> DodgeSuccessCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> PARRY_SUCCESS = CONDITIONS.register("parry_success", () -> ParrySuccessCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> UP = CONDITIONS.register("up", () -> UpCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> DOWN = CONDITIONS.register("down", () -> DownCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> LEFT = CONDITIONS.register("left", () -> LeftCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> RIGHT = CONDITIONS.register("right", () -> RightCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> PRESS_TIME_CONDITION = CONDITIONS.register("press_time_condition", () -> PressedTimeCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> PRESS_INTERVAL_CONDITION = CONDITIONS.register("press_interval_condition", () -> PressIntervalCondition::new);
}
