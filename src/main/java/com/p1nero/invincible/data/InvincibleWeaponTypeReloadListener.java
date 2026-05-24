package com.p1nero.invincible.data;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.capability.item.InvincibleWeaponCapability;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InvincibleWeaponTypeReloadListener extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "capabilities/weapons/invincible_types";

    private static final Gson GSON = new GsonBuilder().create();

    public InvincibleWeaponTypeReloadListener() {
        super(GSON, DIRECTORY);
    }

    @SubscribeEvent
    public static void registerReloadListener(AddReloadListenerEvent event) {
        event.addListener(new InvincibleWeaponTypeReloadListener());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> packEntry, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : packEntry.entrySet()) {
            try {
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                WeaponTypeReloadListener.register(entry.getKey(), deserializeWeaponCapabilityBuilder(entry.getKey(), jsonObject));
            } catch (Exception e) {
                EpicFightMod.LOGGER.warn("Error while deserializing invincible weapon type datapack: {}", entry.getKey(), e);
            }
        }
    }

    public static InvincibleWeaponCapability.Builder deserializeWeaponCapabilityBuilder(ResourceLocation rl, JsonObject jsonObject) throws CommandSyntaxException {
        CompoundTag tag = TagParser.parseTag(jsonObject.toString());
        InvincibleWeaponCapability.Builder builder = InvincibleWeaponCapability.builder();

        if (!tag.contains("category") || tag.getString("category").isBlank()) {
            throw new IllegalArgumentException("Define weapon category.");
        }

        builder.category(WeaponCategory.ENUM_MANAGER.getOrThrow(tag.getString("category")));

        if (tag.contains("collider")) {
            builder.collider(ColliderPreset.deserializeSimpleCollider(tag.getCompound("collider")));
        }

        builder.canBePlacedOffhand(tag.contains("usable_in_offhand") ? tag.getBoolean("usable_in_offhand") : true);

        if (tag.contains("hit_particle")) {
            ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(tag.getString("hit_particle")));
            if (particleType == null) {
                EpicFightMod.LOGGER.warn("Can't find a particle type {} in {}", tag.getString("hit_particle"), rl);
            } else if (!(particleType instanceof HitParticleType hitParticleType)) {
                EpicFightMod.LOGGER.warn("{} is not a hit particle type in {}", tag.getString("hit_particle"), rl);
            } else {
                builder.hitParticle(hitParticleType);
            }
        }

        if (tag.contains("swing_sound")) {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.getString("swing_sound")));
            if (sound == null) {
                EpicFightMod.LOGGER.warn("Can't find a swing sound {} in {}", tag.getString("swing_sound"), rl);
            } else {
                builder.swingSound(sound);
            }
        }

        if (tag.contains("hit_sound")) {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.getString("hit_sound")));
            if (sound == null) {
                EpicFightMod.LOGGER.warn("Can't find a hit sound {} in {}", tag.getString("hit_sound"), rl);
            } else {
                builder.hitSound(sound);
            }
        }

        if (tag.contains("passive_skill")) {
            builder.passiveSkill(SkillManager.getSkill(tag.getString("passive_skill")));
        }

        if (tag.contains("reach")) {
            builder.reach(tag.getFloat("reach"));
        }

        if (tag.contains("consumption")) {
            builder.consumption(tag.getInt("consumption"));
        }

        if (tag.contains("max_stacks")) {
            builder.maxStacks(tag.getInt("max_stacks"));
        }

        if (jsonObject.has("descriptions")) {
            List<String> descriptions = new ArrayList<>();
            for (JsonElement description : jsonObject.getAsJsonArray("descriptions")) {
                descriptions.add(description.getAsString());
            }
            builder.addToolTipOnItem(descriptions);
        }

        if (tag.contains("drawSkillIcon")) {
            builder.drawSkillIcon(tag.getBoolean("drawSkillIcon"));
        }

        if (tag.contains("maxPressTime")) {
            builder.maxPressTime(tag.getInt("maxPressTime"));
        }

        if (tag.contains("maxReserveTime")) {
            builder.maxReserveTime(tag.getInt("maxReserveTime"));
        }

        if (tag.contains("maxProtectTime")) {
            builder.maxProtectTime(tag.getInt("maxProtectTime"));
        }

        if (tag.contains("resetTime")) {
            builder.resetTime(tag.getInt("resetTime"));
        }

        if (tag.contains("skillTextureLocation")) {
            builder.skillTextureLocation(ResourceLocation.parse(tag.getString("skillTextureLocation")));
        }

        if (jsonObject.has("combos")) {
            deserializeCombos(rl, jsonObject.getAsJsonObject("combos"), builder);
        }

        CompoundTag innateSkillsTag = tag.getCompound("innate_skills");
        for (String key : innateSkillsTag.getAllKeys()) {
            Style style = Style.ENUM_MANAGER.getOrThrow(key);
            builder.customCombo(style, itemStack -> SkillManager.getSkill(innateSkillsTag.getString(key)));
        }

        CompoundTag livingMotionModifierTag = tag.getCompound("livingmotion_modifier");
        for (String styleKey : livingMotionModifierTag.getAllKeys()) {
            Style style = Style.ENUM_MANAGER.getOrThrow(styleKey);
            CompoundTag styleAnimationTag = livingMotionModifierTag.getCompound(styleKey);
            for (String livingMotionKey : styleAnimationTag.getAllKeys()) {
                LivingMotion livingMotion = LivingMotion.ENUM_MANAGER.getOrThrow(livingMotionKey);
                String animationId = styleAnimationTag.getString(livingMotionKey);
                AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byKey(animationId);
                if (animation == null) {
                    EpicFightMod.LOGGER.warn("No animation named {} in {}", animationId, rl);
                } else {
                    builder.livingMotionModifier(style, livingMotion, animation);
                }
            }
        }

        CompoundTag stylesTag = tag.getCompound("styles");
        final List<Pair<Predicate<LivingEntityPatch<?>>, Style>> conditions = Lists.newArrayList();
        final Style defaultStyle = Style.ENUM_MANAGER.getOrThrow(stylesTag.getString("default"));
        for (Tag caseTag : stylesTag.getList("cases", Tag.TAG_COMPOUND)) {
            CompoundTag caseCompTag = (CompoundTag) caseTag;
            List<EntityPatchCondition> conditionList = deserializeEntityPatchConditions(caseCompTag.getList("conditions", Tag.TAG_COMPOUND));
            conditions.add(Pair.of(entityPatch -> testConditions(conditionList, entityPatch), Style.ENUM_MANAGER.getOrThrow(caseCompTag.getString("style"))));
        }

        builder.styleProvider(entityPatch -> {
            for (Pair<Predicate<LivingEntityPatch<?>>, Style> entry : conditions) {
                if (entry.getFirst().test(entityPatch)) {
                    return entry.getSecond();
                }
            }
            return defaultStyle;
        });

        if (tag.contains("offhand_item_compatible_predicate")) {
            List<EntityPatchCondition> conditionList = deserializeEntityPatchConditions(tag.getList("offhand_item_compatible_predicate", Tag.TAG_COMPOUND));
            builder.weaponCombinationPredicator(entityPatch -> testConditions(conditionList, entityPatch));
        }

        if (tag.contains("custom_tags")) {
            for (Tag customTag : tag.getList("custom_tags", Tag.TAG_STRING)) {
                builder.addTag(ResourceLocation.parse(customTag.getAsString()));
            }
        }

        builder.addTag(rl);
        return builder;
    }

    private static void deserializeCombos(ResourceLocation rl, JsonObject combosObject, InvincibleWeaponCapability.Builder builder) throws CommandSyntaxException {
        for (Map.Entry<String, JsonElement> comboEntry : combosObject.entrySet()) {
            Style style = Style.ENUM_MANAGER.getOrThrow(comboEntry.getKey());
            JsonArray comboArray = comboEntry.getValue().getAsJsonArray();
            if (comboArray.isEmpty()) {
                continue;
            }

            if (comboArray.get(0).isJsonPrimitive()) {
                builder.autoAttackMotion(style, deserializeAttackAnimations(rl, comboArray));
                continue;
            }

            ComboNode root = ComboNode.create();
            ComboJsonLoader.deserializeCombos(root, comboArray);
            builder.newStyleCombo(style, root);

            List<AnimationAccessor<? extends AttackAnimation>> previewMotions = collectPreviewMotions(rl, comboArray);
            if (!previewMotions.isEmpty()) {
                builder.autoAttackMotion(style, previewMotions);
            }
        }
    }

    private static List<EntityPatchCondition> deserializeEntityPatchConditions(ListTag conditionTags) {
        List<EntityPatchCondition> conditionList = new ArrayList<>();
        for (Tag conditionTag : conditionTags) {
            CompoundTag conditionCompound = (CompoundTag) conditionTag;
            Supplier<EntityPatchCondition> conditionProvider = EpicFightConditions.getConditionOrThrow(ResourceLocation.parse(conditionCompound.getString("predicate")));
            EntityPatchCondition condition = conditionProvider.get();
            condition.read(conditionCompound);
            conditionList.add(condition);
        }
        return conditionList;
    }

    private static boolean testConditions(List<EntityPatchCondition> conditions, LivingEntityPatch<?> entityPatch) {
        for (EntityPatchCondition condition : conditions) {
            if (!condition.predicate(entityPatch)) {
                return false;
            }
        }
        return true;
    }

    private static List<AnimationAccessor<? extends AttackAnimation>> deserializeAttackAnimations(ResourceLocation rl, JsonArray comboArray) {
        List<AnimationAccessor<? extends AttackAnimation>> animations = new ArrayList<>();
        for (JsonElement comboElement : comboArray) {
            if (!comboElement.isJsonPrimitive()) {
                continue;
            }
            addAttackAnimation(animations, comboElement.getAsString(), rl);
        }
        return animations;
    }

    private static List<AnimationAccessor<? extends AttackAnimation>> collectPreviewMotions(ResourceLocation rl, JsonArray comboArray) {
        List<AnimationAccessor<? extends AttackAnimation>> animations = new ArrayList<>();
        for (JsonElement comboElement : comboArray) {
            if (!comboElement.isJsonObject()) {
                continue;
            }

            JsonObject comboObject = comboElement.getAsJsonObject();
            if (comboObject.has("condition_animations")) {
                for (JsonElement conditionAnimation : comboObject.getAsJsonArray("condition_animations")) {
                    JsonObject animationObject = conditionAnimation.getAsJsonObject();
                    if (animationObject.has("animation")) {
                        addAttackAnimation(animations, animationObject.get("animation").getAsString(), rl);
                    }
                }
            } else if (comboObject.has("animation")) {
                addAttackAnimation(animations, comboObject.get("animation").getAsString(), rl);
            }
        }
        return animations;
    }

    private static void addAttackAnimation(List<AnimationAccessor<? extends AttackAnimation>> animations, String animationId, ResourceLocation rl) {
        AnimationAccessor<? extends AttackAnimation> animation = AnimationManager.byKey(animationId);
        if (animation == null) {
            EpicFightMod.LOGGER.warn("Can't find an animation named {} in {}", animationId, rl);
            return;
        }
        animations.add(animation);
    }


    @OnlyIn(Dist.CLIENT)
    public static void processServerPacket(SPDatapackSync packet) {
        //TODO 客户端也注册一份
//        if (packet.getType() == SPDatapackSync.Type.WEAPON_TYPE) {
//            PRESETS.clear();
//            registerDefaultWeaponTypes();
//
//            for(CompoundTag tag : packet.getTags()) {
//                ResourceLocation rl = ResourceLocation.parse(tag.getString("registry_name"));
//
//                try {
//                    PRESETS.put(rl, (Function)(itemstack) -> deserializeWeaponCapabilityBuilder(rl, tag));
//                } catch (Exception e) {
//                    throw new RuntimeException("Weapon type " + String.valueOf(rl) + " encountered an error", e);
//                }
//            }
//
//            ItemCapabilityReloadListener.weaponTypeProcessedCheck();
//        }

    }

}
