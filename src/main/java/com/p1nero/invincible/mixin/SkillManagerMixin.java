package com.p1nero.invincible.mixin;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.data.SkillLoader;
import com.p1nero.invincible.gameassets.InvincibleSkills;
import com.p1nero.invincible.skill.ComboBasicAttack;
import com.p1nero.invincible.skill.api.ComboNode;
import com.p1nero.invincible.skill.api.ComboType;
import net.minecraft.nbt.*;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.io.IOException;
import java.util.Map;

import static yesman.epicfight.api.data.reloader.SkillManager.getSkillRegistry;

/**
 * 从数据包注册技能
 * 感谢SettingDust提供帮助
 */
@Mixin(value = SkillManager.class, remap = false)
public abstract class SkillManagerMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void invincible$addSkillRegistry(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profileFiller, CallbackInfo ci) {

        try {
            for (Resource resource : FileToIdConverter.json("capabilities/weapons/invincible_combos").listMatchingResources(resourceManager).values()) {
                JsonReader jsonReader = new JsonReader(resource.openAsReader());
                jsonReader.setLenient(true);//允许注释
                JsonObject weapon = JsonParser.parseReader(jsonReader).getAsJsonObject();
                SkillLoader.loadSkill(weapon);
                InvincibleSkills.NEW_SKILLS.add(TagParser.parseTag(weapon.toString()));//保存下来，发包用
                jsonReader.close();
            }
        } catch (IOException | CommandSyntaxException exception) {
            InvincibleMod.LOGGER.error("failed to load invincible_combos in server side", exception);
        }
    }

}
