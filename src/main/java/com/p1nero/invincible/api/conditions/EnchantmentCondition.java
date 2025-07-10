package com.p1nero.invincible.api.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class EnchantmentCondition implements Condition<ServerPlayerPatch> {
    private boolean isMainHand;
    private ResourceKey<Enchantment> effectKey;
    private int min, max;
    public EnchantmentCondition(){

    }

    public EnchantmentCondition(boolean isMainHand, ResourceLocation effectKey, int min, int max) {
        this.isMainHand = isMainHand;
        this.effectKey = createKey(effectKey);
        this.min = min;
        this.max = max;
    }

    public EnchantmentCondition(boolean isMainHand, ResourceLocation effectKey, int level) {
        this.isMainHand = isMainHand;
        this.effectKey = createKey(effectKey);
        this.min = level;
        this.max = level;
    }

    private static ResourceKey<Enchantment> createKey(ResourceLocation resourceLocation){
        return ResourceKey.create(Registries.ENCHANTMENT, resourceLocation);
    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        this.isMainHand = compoundTag.getBoolean("is_main_hand");
        this.effectKey = createKey(ResourceLocation.parse(compoundTag.getString("enchantment")));
        this.min = compoundTag.getInt("min");
        this.max = compoundTag.getInt("max");
        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("is_main_hand", isMainHand);
        tag.putString("enchantment", effectKey.toString());
        tag.putInt("min", min);
        tag.putInt("max", max);
        return tag;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        RegistryAccess registryAccess = serverPlayerPatch.getLevel().registryAccess();
        int level = EnchantmentHelper.getTagEnchantmentLevel(registryAccess.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(effectKey), isMainHand ? serverPlayerPatch.getOriginal().getMainHandItem() : serverPlayerPatch.getOriginal().getOffhandItem());
        return  level >= min && level <= max;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }
}