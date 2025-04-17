package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.conditions.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.data.conditions.Condition;

import java.util.function.Supplier;

public class InvincibleConditions {
    public static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath("epicfight", "conditions"), InvincibleMod.MOD_ID);
    public static final RegistryObject<Supplier<Condition<?>>> JUMP_CONDITION = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "jumping")).getPath(), () -> JumpCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> DASH_CONDITION = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "sprinting")).getPath(), () -> SprintingCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> STACK_CONDITION = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "stack_count")).getPath(), () -> StackCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> PHASE = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "phase")).getPath(), () -> PlayerPhaseCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> COOLDOWN = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "cooldown")).getPath(), () -> CooldownCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> MOB_EFFECT = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "mob_effect")).getPath(), () -> MobEffectCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> ENCHANTMENT = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "enchantment")).getPath(), () -> EnchantmentCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> BLOCKING = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "blocking")).getPath(), () -> BlockingCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> TARGET_BLOCKING = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "target_blocking")).getPath(), () -> TargetBlockingCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> HAS_VEHICLE = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "has_vehicle")).getPath(), () -> VehicleCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> DODGE_SUCCESS = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "dodge_success")).getPath(), () -> DodgeSuccessCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> PARRY_SUCCESS = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "parry_success")).getPath(), () -> ParrySuccessCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> UP = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "up")).getPath(), () -> UpCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> DOWN = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "down")).getPath(), () -> DownCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> LEFT = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "left")).getPath(), () -> LeftCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> RIGHT = CONDITIONS.register((ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "right")).getPath(), () -> RightCondition::new);

}
