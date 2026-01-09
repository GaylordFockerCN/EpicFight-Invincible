package com.p1nero.invincible.mixin;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(EntityPatchProvider.class)
public interface EntityPatchProviderAccessor {

    @Accessor(value = "CAPABILITIES", remap = false)
    static Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> getCapabilities() {
        return Maps.newHashMap();
    }


    @Accessor(value = "CUSTOM_CAPABILITIES", remap = false)
    static Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> getCustomCapabilities() {
        return Maps.newHashMap();
    }
}
