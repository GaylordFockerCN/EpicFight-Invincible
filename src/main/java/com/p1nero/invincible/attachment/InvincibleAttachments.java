package com.p1nero.invincible.attachment;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.function.Supplier;

@EventBusSubscriber(modid = InvincibleMod.MOD_ID)
public class InvincibleAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, InvincibleMod.MOD_ID);

    public static final Supplier<AttachmentType<InvinciblePlayer>> INVINCIBLE_PLAYER = ATTACHMENT_TYPES.register(
            "invincible_player", () -> AttachmentType.builder(InvinciblePlayer::new).build()
    );

    public static final Supplier<AttachmentType<InvincibleEntity>> INVINCIBLE_ENTITY = ATTACHMENT_TYPES.register(
            "invincible_entity", () -> AttachmentType.builder(InvincibleEntity::new).build()
    );

    public static InvinciblePlayer getPlayer(Player player) {
        return player.getData(INVINCIBLE_PLAYER);
    }

    public static InvinciblePlayer getPlayer(PlayerPatch<?> playerPatch) {
        return getPlayer(playerPatch.getOriginal());
    }

    public static InvincibleEntity getEntity(LivingEntity entity) {
        return entity.getData(INVINCIBLE_ENTITY);
    }

    public static InvincibleEntity getEntity(LivingEntityPatch<?> entityPatch) {
        return getEntity(entityPatch.getOriginal());
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal().hasData(INVINCIBLE_PLAYER)) {
            event.getEntity().getData(INVINCIBLE_PLAYER).copyFrom(event.getOriginal().getData(INVINCIBLE_PLAYER));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        getPlayer(event.getEntity()).tick();
    }

}
