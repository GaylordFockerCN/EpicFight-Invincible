package com.p1nero.invincible.network.packet;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.data.SkillLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nullable;

public record SyncSkillRegistryPacket(CompoundTag data) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(data);
    }

    public static SyncSkillRegistryPacket decode(FriendlyByteBuf buf) {
        return new SyncSkillRegistryPacket(buf.readNbt());
    }

    @Override
    public void execute(@Nullable Player player) {
        InvincibleMod.LOGGER.info("received server new skill registry packet, starting sync.");
        try {
            SkillLoader.loadSkill(data);
        } catch (CommandSyntaxException e) {
            InvincibleMod.LOGGER.error("failed to deserialize combo data in client.", e);
        }
    }

}