package com.p1nero.invincible.events;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.gameassets.InvincibleSkills;
import com.p1nero.invincible.network.PacketHandler;
import com.p1nero.invincible.network.packet.SyncSkillRegistryPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.entity.player.PlayerNegotiationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID)
public class ForgeEvents {

    @SubscribeEvent
    public static void onPlayerNegotiationEvent(final PlayerNegotiationEvent event) {
        InvincibleMod.LOGGER.info("sending sync skill packet.");
        for (CompoundTag data : InvincibleSkills.NEW_SKILLS) {
            PacketHandler.INSTANCE.sendTo(new SyncSkillRegistryPacket(data), event.getConnection(), NetworkDirection.LOGIN_TO_CLIENT);
        }

    }


}
