package com.p1nero.invincible.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class MobEffectCondition implements Condition<ServerPlayerPatch> {
    private boolean isTarget;
    private Supplier<MobEffect> effectSupplier;
    private int min, max;

    public MobEffectCondition() {

    }

    public MobEffectCondition(boolean isTarget, Supplier<MobEffect> effectSupplier, int min, int max) {
        this.isTarget = isTarget;
        this.effectSupplier = effectSupplier;
        this.min = min;
        this.max = max;
    }

    public MobEffectCondition(boolean isTarget, Supplier<MobEffect> effectSupplier, int level) {
        this.isTarget = isTarget;
        this.effectSupplier = effectSupplier;
        this.min = level;
        this.max = level;
    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        this.isTarget = compoundTag.getBoolean("is_target");
        this.effectSupplier = () -> ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(compoundTag.getString("effect")));
        this.min = compoundTag.getInt("min");
        this.max = compoundTag.getInt("max");
        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("is_target", isTarget);
        tag.putString("effect", Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getKey(effectSupplier.get())).toString());
        tag.putInt("min", min);
        tag.putInt("max", max);
        return tag;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        if(isTarget){
            if(serverPlayerPatch.getTarget() == null){
                return false;
            }
            return test(serverPlayerPatch.getTarget());
        } else {
            return test(serverPlayerPatch.getOriginal());
        }
    }

    public boolean test(LivingEntity livingEntity){
        if(!livingEntity.hasEffect(effectSupplier.get())){
            return false;
        }
        int level = livingEntity.getEffect(effectSupplier.get()).getAmplifier();
        return  level >= min && level <= max;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }
}