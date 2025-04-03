package com.p1nero.invincible.conditions;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.function.Supplier;

/**
 * 检测手上物品附魔的等级
 */
public class EnchantmentCondition implements Condition<ServerPlayerPatch>{
    private final boolean isMainHand;
    private final Supplier<Enchantment> enchantmentSupplier;
    private final int min, max;

    /**
     * @param isMainHand 是否主手，false则检测副手
     * @param enchantmentSupplier 附魔种类
     * @param min 最小附魔等级
     * @param max 最大附魔等级
     */
    public EnchantmentCondition(boolean isMainHand, Supplier<Enchantment> enchantmentSupplier, int min, int max) {
        this.isMainHand = isMainHand;
        this.enchantmentSupplier = enchantmentSupplier;
        this.min = min;
        this.max = max;
    }
    /**
     * @param isMainHand 是否主手，false则检测副手
     * @param enchantmentSupplier 附魔种类
     * @param level 附魔等级
     */
    public EnchantmentCondition(boolean isMainHand, Supplier<Enchantment> enchantmentSupplier, int level) {
        this.isMainHand = isMainHand;
        this.enchantmentSupplier = enchantmentSupplier;
        this.min = level;
        this.max = level;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(enchantmentSupplier.get(), isMainHand ? serverPlayerPatch.getOriginal().getMainHandItem() : serverPlayerPatch.getOriginal().getOffhandItem());
        return  level >= min && level <= max;
    }
}