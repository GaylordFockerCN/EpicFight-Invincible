package com.p1nero.invincible.damagesource;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public interface InvincibleDamageTypeTags {

    //带此标签的伤害源将不会对invincible武器进行充能
    TagKey<DamageType> NOT_CHARGE = create("not_charge");

    private static TagKey<DamageType> create(String tagName) {
        return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(InvincibleMod.MOD_ID, tagName));
    }
}
