package com.p1nero.invincible.mixin;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AdvancedCustomHumanoidMobPatch.class, remap = false)
public interface AdvancedCustomHumanoidMobPatchAccessor {

    @Accessor("isParry")
    boolean isParry();
}
