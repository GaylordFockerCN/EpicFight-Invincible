package com.p1nero.invincible.item;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class InvincibleItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, InvincibleMod.MOD_ID);
    public static final RegistryObject<Item> DEBUG = ITEMS.register("debug", () -> new SwordItem(Tiers.WOOD, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> CUSTOM_COMBO_DEMO = ITEMS.register("custom_combo_demo", () -> new SwordItem(Tiers.WOOD, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> CUSTOM_SKILL_DEMO = ITEMS.register("custom_skill_demo", () -> new SwordItem(Tiers.WOOD, 3, -2.4F, new Item.Properties()));

}
