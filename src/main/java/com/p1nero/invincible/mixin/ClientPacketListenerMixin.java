package com.p1nero.invincible.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.registry.entries.EpicFightParticles;

/**
 * 出生Mojang sendParticle会暗改参数
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Shadow private ClientLevel level;

    @Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true)
    private void invincible$handleEntityAfterImageParticle(ClientboundLevelParticlesPacket packet, CallbackInfo ci){
        PacketUtils.ensureRunningOnSameThread(packet, (ClientPacketListener)(Object)this, Minecraft.getInstance());
        if(packet.getParticle().equals(EpicFightParticles.ENTITY_AFTER_IMAGE.get())){
            this.level.addParticle(EpicFightParticles.ENTITY_AFTER_IMAGE.get(), packet.getX(), packet.getY(), packet.getZ(), Double.longBitsToDouble(((long) packet.getMaxSpeed())), 0.0, 0.0);
            ci.cancel();
        }
    }
}
