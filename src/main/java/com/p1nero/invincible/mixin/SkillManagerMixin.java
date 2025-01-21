package com.p1nero.invincible.mixin;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.skill.ComboBasicAttack;
import com.p1nero.invincible.skill.api.ComboNode;
import com.p1nero.invincible.skill.api.ComboType;
import net.minecraft.nbt.*;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
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
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.io.IOException;
import java.util.Map;

import static yesman.epicfight.api.data.reloader.SkillManager.getSkillRegistry;

/**
 * 从数据包注册技能
 * 感谢SettingDust提供帮助
 */
@SuppressWarnings("UnstableApiUsage")
@Mixin(value = SkillManager.class, remap = false)
public abstract class SkillManagerMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void invincible$addSkillRegistry(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profileFiller, CallbackInfo ci) {
        IForgeRegistry<Skill> skillRegistry = getSkillRegistry();
        if (skillRegistry instanceof ForgeRegistry<Skill> registry) {
            registry.unfreeze();
            InvincibleMod.LOGGER.warn("unfreezing Skill Registry.");
            try {
                for (Resource resource : FileToIdConverter.json("invincible_combos").listMatchingResources(resourceManager).values()) {
                    JsonReader jsonReader = new JsonReader(resource.openAsReader());
                    jsonReader.setLenient(true);
                    JsonObject weapon = JsonParser.parseReader(resource.openAsReader()).getAsJsonObject();
                    String modId = weapon.get("mod_id").getAsString();
                    String name = weapon.get("name").getAsString();

                    boolean drawSkillIcon = false;
                    if (weapon.has("drawSkillIcon")) {
                        drawSkillIcon = weapon.get("drawSkillIcon").getAsBoolean();
                    }

                    JsonArray combos = weapon.getAsJsonArray("combos");

                    ComboNode root = ComboNode.createRoot();
                    invincible$deserializeCombos(root, combos);

                    ComboBasicAttack.Builder builder = ((ComboBasicAttack.Builder) ComboBasicAttack.createComboBasicAttack()
                            .setShouldDrawGui(drawSkillIcon)
                            .setCombo(root)
                            .setRegistryName(new ResourceLocation(modId, name)));

                    ComboBasicAttack skill = new ComboBasicAttack(builder);
                    CompoundTag params = new CompoundTag();
                    if(weapon.has("consumption")){
                        params.putFloat("consumption", weapon.get("consumption").getAsFloat());
                    }
                    if(weapon.has("max_stacks")){
                        params.putInt("max_stacks", weapon.get("max_stacks").getAsInt());
                    }
                    skill.setParams(params);
                    registry.register(skill.getRegistryName(), skill);
                }
            } catch (IOException | CommandSyntaxException ioException) {
                InvincibleMod.LOGGER.error("failed to load invincible_combos", ioException);
            }

            registry.freeze();
            InvincibleMod.LOGGER.warn("freezing Skill Registry.");
        }
    }

    /**
     * 解析连击树
     */
    @Unique
    private void invincible$deserializeCombos(ComboNode parent, JsonArray combos) throws CommandSyntaxException {

        for (JsonElement comboElement : combos) {
            JsonObject combo = comboElement.getAsJsonObject();
            String key = combo.get("key").getAsString();
            String animation = combo.get("animation").getAsString();
            ComboNode node = ComboNode.createNode(() -> AnimationManager.getInstance().byKeyOrThrow(animation));

            if(combo.has("play_speed")){
                node.setPlaySpeed(combo.get("play_speed").getAsFloat());
            }

            if(combo.has("convert_time")){
                node.setPlaySpeed(combo.get("convert_time").getAsFloat());
            }

            if(combo.has("not_charge")){
                node.setNotCharge(combo.get("not_charge").getAsBoolean());
            }

            //获取判断条件
            if(combo.has("conditions")){
                JsonArray conditionList = combo.getAsJsonArray("conditions");
                for (JsonElement conditionElement : conditionList) {
                    JsonObject condition = conditionElement.getAsJsonObject();
//                CompoundTag tag = ((CompoundTag) ExtraCodecs.JSON.encodeStart(NbtOps.INSTANCE, condition).result().get());
                    CompoundTag tag = TagParser.parseTag(condition.toString());
                    Condition<? extends LivingEntityPatch<?>> predicate = MobPatchReloadListener.deserializeBehaviorPredicate(tag.getString("predicate"), tag);
                    node.setCondition(predicate);
                }
            }

            //获取命令列表
            if(combo.has("command_list")){
                JsonArray commandList = combo.getAsJsonArray("command_list");
                for (JsonElement commandElement : commandList) {
                    JsonObject command = commandElement.getAsJsonObject();
                    float time = command.get("time").getAsFloat();
                    String commandText = command.get("command").getAsString();
                    boolean executeAtTarget = command.get("execute_at_target").getAsBoolean();
                    node.addTimeEvent(TimeStampedEvent.createTimeCommandEvent(time, commandText, executeAtTarget));
                }
            }

            //递归构造
            if (combo.has("combos")) {
                invincible$deserializeCombos(node, combo.getAsJsonArray("combos"));
            }

            ComboType keyType = ComboNode.ComboTypes.valueOf(key);
            parent.addChild(keyType, node);
        }
    }

}
