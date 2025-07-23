package com.p1nero.invincible.client.particles;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class InvincibleParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, InvincibleMod.MOD_ID);
    public static final RegistryObject<SimpleParticleType> TRANSPARENT_AFTER_IMAGE = PARTICLES.register("transparent_after_image", () -> new SimpleParticleType(true));
}