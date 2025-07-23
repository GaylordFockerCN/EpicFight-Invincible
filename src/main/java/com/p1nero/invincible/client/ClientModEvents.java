package com.p1nero.invincible.client;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.client.particles.InvincibleParticles;
import com.p1nero.invincible.client.particles.TransparentEntityAfterImageParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onParticleRegistry(final RegisterParticleProvidersEvent event) {
        event.registerSpecial(InvincibleParticles.TRANSPARENT_AFTER_IMAGE.get(), new TransparentEntityAfterImageParticle.Provider());
    }
}
