package com.p1nero.invincible.capability.item;

import com.google.common.collect.Lists;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class ComboWeaponCapability extends WeaponCapability {
    protected ComboWeaponCapability(Builder builder) {
        super(builder);
        this.autoAttackMotions.put(CapabilityItem.Styles.COMMON, Lists.newArrayList(Animations.SWORD_AIR_SLASH, Animations.SWORD_AIR_SLASH, Animations.SWORD_AIR_SLASH));
    }
}