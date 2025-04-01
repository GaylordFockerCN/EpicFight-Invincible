package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.function.Function;

/**
 * 需要先注册技能，参考{@link InvincibleSkills}
 */
@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InvincibleWeaponCapabilityPresets {

    //It's easy to create a new weapon type, just need to provide the innate skill. newStyleCombo should be set.
    //注册非常简单，newStyleCombo需要随便填一下，但是选择武器技能是必要的
    public static final Function<Item, CapabilityItem.Builder> DEMO = (item) ->
            (CapabilityItem.Builder) WeaponCapability.builder().category(CapabilityItem.WeaponCategories.SWORD)
                    .styleProvider((entityPatch) -> CapabilityItem.Styles.COMMON)
                    .collider(ColliderPreset.SWORD)
                    .swingSound(EpicFightSounds.WHOOSH.get())
                    .hitSound(EpicFightSounds.BLADE_HIT.get())
                    .hitParticle(EpicFightParticles.HIT_BLADE.get())
                    .canBePlacedOffhand(false)
                    .newStyleCombo(CapabilityItem.Styles.COMMON, Animations.SWORD_AIR_SLASH)//随便设一个 fill it casually
                    .innateSkill(CapabilityItem.Styles.COMMON, (itemstack) -> InvincibleSkills.COMBO_DEMO)
                    .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.BLOCK, Animations.BIPED_BLOCK)
                    .comboCancel((style) -> false);

    @SubscribeEvent
    public static void register(WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(new ResourceLocation(InvincibleMod.MOD_ID, "demo"), DEMO);
    }

}
