package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.conditions.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.data.conditions.Condition;

import java.util.function.Supplier;

public class InvincibleConditions {
    public static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS = DeferredRegister.create(new ResourceLocation("epicfight", "conditions"), InvincibleMod.MOD_ID);
    public static final RegistryObject<Supplier<Condition<?>>> JUMP_CONDITION = CONDITIONS.register("jumping", () -> JumpCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> DASH_CONDITION = CONDITIONS.register("sprinting", () -> SprintingCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> STACK_CONDITION = CONDITIONS.register("stack_count", () -> StackCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> PHASE = CONDITIONS.register("phase", () -> PlayerPhaseCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> COOLDOWN = CONDITIONS.register("cooldown", () -> CooldownCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> BLOCKING = CONDITIONS.register("blocking", () -> BlockingCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> TARGET_BLOCKING = CONDITIONS.register("target_blocking", () -> TargetBlockingCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> HAS_VEHICLE = CONDITIONS.register("has_vehicle", () -> VehicleCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> DODGE_SUCCESS = CONDITIONS.register("dodge_success", () -> DodgeSuccessCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> PARRY_SUCCESS = CONDITIONS.register("parry_success", () -> ParrySuccessCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> UP = CONDITIONS.register("up", () -> UpCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> DOWN = CONDITIONS.register("down", () -> DownCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> LEFT = CONDITIONS.register("left", () -> LeftCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> RIGHT = CONDITIONS.register("right", () -> RightCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> ENCHANTMENT = CONDITIONS.register("enchantment", () -> EnchantmentCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> MOB_EFFECT = CONDITIONS.register("mob_effect", () -> MobEffectCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> PRESS_TIME_CONDITION = CONDITIONS.register("press_time_condition", () -> PressedTimeCondition::new);
}
