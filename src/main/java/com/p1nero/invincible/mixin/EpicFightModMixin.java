package com.p1nero.invincible.mixin;

import com.p1nero.invincible.api.forgeevent.DuplicateAnimationRegistryEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.main.EpicFightMod;

import java.util.Comparator;

@Mixin(EpicFightMod.class)
public class EpicFightModMixin {

    /**
     * forge事件优先级无效，只能mixin否则崩溃= =
     */
    @Inject(method = "constructMod", at = @At("TAIL"), remap = false)
    private void invincible$constructMod(FMLConstructModEvent event, CallbackInfo ci) {
        event.enqueueWork(() -> {
            DuplicateAnimationRegistryEvent registryEvent = new DuplicateAnimationRegistryEvent();
            ModLoader.get().postEvent(registryEvent);
            registryEvent.getBuilders().stream().sorted(Comparator.comparing(DuplicateAnimationRegistryEvent.DuplicateAnimationBuilder::namespace)).forEach((builder) -> builder.task().accept(builder));
        });
    }
}
