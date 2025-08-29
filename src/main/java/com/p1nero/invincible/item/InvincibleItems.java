package com.p1nero.invincible.item;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InvincibleItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(InvincibleMod.MOD_ID);
    public static final DeferredItem<Item> DEBUG = ITEMS.register("debug", () -> new SwordItem(Tiers.WOOD, new Item.Properties()));
    public static final DeferredItem<Item> CUSTOM_COMBO_DEMO = ITEMS.register("custom_combo_demo", () -> new SwordItem(Tiers.WOOD, new Item.Properties()));
    public static final DeferredItem<Item> CUSTOM_SKILL_DEMO = ITEMS.register("custom_skill_demo", () -> new SwordItem(Tiers.WOOD, new Item.Properties()));

}
