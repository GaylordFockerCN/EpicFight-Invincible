package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.conditions.JumpCondition;
import com.p1nero.invincible.conditions.SprintingCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.data.conditions.Condition;

import java.util.function.Supplier;

public class InvincibleConditions {
    public static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS = DeferredRegister.create(new ResourceLocation("epicfight", "conditions"), InvincibleMod.MOD_ID);
    public static final RegistryObject<Supplier<Condition<?>>> JUMP_CONDITION = CONDITIONS.register((new ResourceLocation(InvincibleMod.MOD_ID, "jumping")).getPath(), () -> JumpCondition::new);
    public static final RegistryObject<Supplier<Condition<?>>> DASH_CONDITION = CONDITIONS.register((new ResourceLocation(InvincibleMod.MOD_ID, "sprinting")).getPath(), () -> SprintingCondition::new);

}
