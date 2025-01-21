package com.p1nero.invincible.item;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class InvincibleItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, InvincibleMod.MOD_ID);
    public static final RegistryObject<Item> DEBUG = ITEMS.register("debug", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DATAPACK_DEBUG = ITEMS.register("datapack_debug", () -> new Item(new Item.Properties()));

}
