package com.p1nero.invincible.capabilities.item;

import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class ComboWeaponCapability {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends WeaponCapability.Builder{
        protected Builder() {
            super();
            this.newStyleCombo(CapabilityItem.Styles.COMMON, Animations.SWORD_AUTO1, Animations.SWORD_AUTO1, Animations.SWORD_AUTO1);
        }

    }
}
