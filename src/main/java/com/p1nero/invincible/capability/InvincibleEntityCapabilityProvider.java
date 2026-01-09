package com.p1nero.invincible.capability;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID)
public class InvincibleEntityCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<InvincibleEntity> INVINCIBLE_ENTITY = CapabilityManager.get(new CapabilityToken<>() {});

    private InvincibleEntity InvincibleEntity = null;
    
    private final LazyOptional<InvincibleEntity> optional = LazyOptional.of(this::createInvincibleEntity);

    private InvincibleEntity createInvincibleEntity() {
        if(this.InvincibleEntity == null){
            this.InvincibleEntity = new InvincibleEntity();
        }

        return this.InvincibleEntity;
    }

    public static InvincibleEntity get(LivingEntity entity){
        return entity.getCapability(INVINCIBLE_ENTITY).orElse(new InvincibleEntity().err());
    }

    public static InvincibleEntity get(LivingEntityPatch<?> patch){
        return get(patch.getOriginal());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        if(capability == INVINCIBLE_ENTITY){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        createInvincibleEntity().saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        createInvincibleEntity().loadNBTData(tag);
    }

}
