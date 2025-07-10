package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.conditions.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.data.conditions.Condition;

import java.util.function.Supplier;

public class InvincibleConditions {
    public static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath("epicfight", "conditions"), InvincibleMod.MOD_ID);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> JUMP_CONDITION = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "jumping")).getPath(), () -> JumpCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> DASH_CONDITION = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "sprinting")).getPath(), () -> SprintingCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> STACK_CONDITION = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "stack_count")).getPath(), () -> StackCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> PHASE = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "phase")).getPath(), () -> PlayerPhaseCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> COOLDOWN = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "cooldown")).getPath(), () -> CooldownCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> MOB_EFFECT = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "mob_effect")).getPath(), () -> MobEffectCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> ENCHANTMENT = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "enchantment")).getPath(), () -> EnchantmentCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> BLOCKING = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "blocking")).getPath(), () -> BlockingCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> TARGET_BLOCKING = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "target_blocking")).getPath(), () -> TargetBlockingCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> HAS_VEHICLE = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "has_vehicle")).getPath(), () -> VehicleCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> DODGE_SUCCESS = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "dodge_success")).getPath(), () -> DodgeSuccessCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> PARRY_SUCCESS = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "parry_success")).getPath(), () -> ParrySuccessCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> UP = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "up")).getPath(), () -> UpCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> DOWN = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "down")).getPath(), () -> DownCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> LEFT = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "left")).getPath(), () -> LeftCondition::new);
    public static final DeferredHolder<Supplier<Condition<?>>, Supplier<Condition<?>>> RIGHT = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "right")).getPath(), () -> RightCondition::new);

}
