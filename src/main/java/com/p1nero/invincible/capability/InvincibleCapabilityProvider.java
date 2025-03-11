package com.p1nero.invincible.capability;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID)
public class InvincibleCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<InvinciblePlayer> INVINCIBLE_PLAYER = CapabilityManager.get(new CapabilityToken<>() {});

    private InvinciblePlayer invinciblePlayer = null;
    
    private final LazyOptional<InvinciblePlayer> optional = LazyOptional.of(this::createInvinciblePlayer);

    private InvinciblePlayer createInvinciblePlayer() {
        if(this.invinciblePlayer == null){
            this.invinciblePlayer = new InvinciblePlayer();
        }

        return this.invinciblePlayer;
    }

    public static InvinciblePlayer get(Player player){
        return player.getCapability(INVINCIBLE_PLAYER).orElse(new InvinciblePlayer());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        if(capability == INVINCIBLE_PLAYER){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        createInvinciblePlayer().saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        createInvinciblePlayer().loadNBTData(tag);
    }

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(InvincibleCapabilityProvider.INVINCIBLE_PLAYER).isPresent()){
                event.addCapability(new ResourceLocation(InvincibleMod.MOD_ID, "invincible_player"), new InvincibleCapabilityProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        if(event.isWasDeath()) {
            event.getOriginal().getCapability(InvincibleCapabilityProvider.INVINCIBLE_PLAYER).ifPresent(oldStore -> {
                event.getEntity().getCapability(InvincibleCapabilityProvider.INVINCIBLE_PLAYER).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        get(event.player).tick();
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(InvinciblePlayer.class);
    }


}
