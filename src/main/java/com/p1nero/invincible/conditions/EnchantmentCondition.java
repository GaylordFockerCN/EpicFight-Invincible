package com.p1nero.invincible.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class EnchantmentCondition implements Condition<ServerPlayerPatch> {
    private boolean isMainHand;
    private Supplier<Enchantment> effectSupplier;
    private int min, max;
    public EnchantmentCondition(){

    }

    public EnchantmentCondition(boolean isMainHand, Supplier<Enchantment> effectSupplier, int min, int max) {
        this.isMainHand = isMainHand;
        this.effectSupplier = effectSupplier;
        this.min = min;
        this.max = max;
    }

    public EnchantmentCondition(boolean isMainHand, Supplier<Enchantment> effectSupplier, int level) {
        this.isMainHand = isMainHand;
        this.effectSupplier = effectSupplier;
        this.min = level;
        this.max = level;
    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        this.isMainHand = compoundTag.getBoolean("is_main_hand");
        this.effectSupplier = () -> ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(compoundTag.getString("enchantment")));
        this.min = compoundTag.getInt("min");
        this.max = compoundTag.getInt("max");
        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("is_main_hand", isMainHand);
        tag.putString("enchantment", Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(effectSupplier.get())).toString());
        tag.putInt("min", min);
        tag.putInt("max", max);
        return tag;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        int level = EnchantmentHelper.getTagEnchantmentLevel(effectSupplier.get(), isMainHand ? serverPlayerPatch.getOriginal().getMainHandItem() : serverPlayerPatch.getOriginal().getOffhandItem());
        return  level >= min && level <= max;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }
}