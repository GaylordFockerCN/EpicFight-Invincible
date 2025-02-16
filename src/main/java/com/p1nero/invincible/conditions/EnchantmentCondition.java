package com.p1nero.invincible.conditions;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * 检测手上物品附魔的等级
 */
public class EnchantmentCondition implements Condition<ServerPlayerPatch>{
    private final boolean isMainHand;
    private final Enchantment enchantment;
    private final int enchantmentLevel;

    /**
     *
     * @param isMainHand 是否主手，false则检测副手
     * @param enchantment 附魔种类
     * @param enchantmentLevel 附魔等级
     */
    public EnchantmentCondition(boolean isMainHand, Enchantment enchantment, int enchantmentLevel) {
        this.isMainHand = isMainHand;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, isMainHand ? serverPlayerPatch.getOriginal().getMainHandItem() : serverPlayerPatch.getOriginal().getOffhandItem()) == enchantmentLevel;
    }

}
