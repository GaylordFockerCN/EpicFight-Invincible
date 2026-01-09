package com.p1nero.invincible.capability;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.mixin.EntityPatchProviderAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID)
public class InvincibleCapabilities {

    public static InvincibleEntity getEntityCap(LivingEntity entity){
        return entity.getCapability(InvincibleEntityCapabilityProvider.INVINCIBLE_ENTITY).orElse(new InvincibleEntity().err());
    }

    public static InvinciblePlayer getPlayerCap(Player player){
        return player.getCapability(InvinciblePlayerCapabilityProvider.INVINCIBLE_PLAYER).orElse(new InvinciblePlayer().err());
    }

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity living
                && (EntityPatchProviderAccessor.getCustomCapabilities().containsKey(living.getType())
                || EntityPatchProviderAccessor.getCapabilities().containsKey(living.getType()))) {
            if(!event.getObject().getCapability(InvincibleEntityCapabilityProvider.INVINCIBLE_ENTITY).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "invincible_entity"), new InvincibleEntityCapabilityProvider());
            }
        }
        if (event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(InvinciblePlayerCapabilityProvider.INVINCIBLE_PLAYER).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, "invincible_player"), new InvinciblePlayerCapabilityProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(InvinciblePlayer.class);
        event.register(InvincibleEntity.class);
    }
}
