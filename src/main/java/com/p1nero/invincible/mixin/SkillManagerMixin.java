package com.p1nero.invincible.mixin;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.events.BiEvent;
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
                for (Resource resource : FileToIdConverter.json("capabilities/weapons/invincible_combos").listMatchingResources(resourceManager).values()) {
                    JsonReader jsonReader = new JsonReader(resource.openAsReader());
                    jsonReader.setLenient(true);//允许注释
                    JsonObject weapon = JsonParser.parseReader(jsonReader).getAsJsonObject();
                    String modId = weapon.get("mod_id").getAsString();
                    String name = weapon.get("name").getAsString();
                    //防止读到模板
                    if (modId.isEmpty() || (FMLEnvironment.dist.isDedicatedServer() && name.equals("datapack_demo"))) {
                        continue;
                    }
                    boolean drawSkillIcon = false;
                    if (weapon.has("drawSkillIcon")) {
                        drawSkillIcon = weapon.get("drawSkillIcon").getAsBoolean();
                    }

                    JsonArray combos = weapon.getAsJsonArray("combos");

                    ComboNode root = ComboNode.create();
                    invincible$deserializeCombos(root, combos);

                    ComboBasicAttack.Builder builder = ((ComboBasicAttack.Builder) ComboBasicAttack.createComboBasicAttack()
                            .setShouldDrawGui(drawSkillIcon)
                            .setCombo(root)
                            .setRegistryName(new ResourceLocation(modId, name)));

                    ComboBasicAttack skill = new ComboBasicAttack(builder);
                    CompoundTag params = new CompoundTag();
                    if (weapon.has("consumption")) {
                        params.putFloat("consumption", weapon.get("consumption").getAsFloat());
                    }
                    if (weapon.has("max_stacks")) {
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
            ComboNode child = ComboNode.create();

            //有condition_animations说明为特殊类型，要借递归进行特殊处理
            if(combo.has("condition_animations")){
                JsonArray conditionAnimationsListList = combo.getAsJsonArray("condition_animations");
                //把自己传进去，解析以保存各种可能的动画参数
                invincible$deserializeCombos(child, conditionAnimationsListList);
            } else {
                String animation = combo.get("animation").getAsString();
                child.setAnimationProvider(() -> AnimationManager.getInstance().byKeyOrThrow(animation));

                if (combo.has("speed_multiplier")) {
                    child.setPlaySpeed(combo.get("speed_multiplier").getAsFloat());
                }

                if (combo.has("damage_multiplier")) {
                    JsonObject valueModifier = combo.getAsJsonObject("damage_multiplier");
                    float adder = 0, multiplier = 1.0F, setter = Float.NaN;
                    if(valueModifier.has("adder")){
                        adder = valueModifier.get("adder").getAsFloat();
                    }
                    if(valueModifier.has("multiplier")){
                        multiplier = valueModifier.get("multiplier").getAsFloat();
                    }
                    if(valueModifier.has("setter")){
                        setter = valueModifier.get("setter").getAsFloat();
                    }
                    child.setDamageMultiplier(new ValueModifier(adder, multiplier, setter));
                }

                if (combo.has("hurt_damage_multiplier")) {
                    child.setHurtDamageMultiplier(combo.get("hurt_damage_multiplier").getAsFloat());
                }

                if (combo.has("impact_multiplier")) {
                    child.setImpactMultiplier(combo.get("impact_multiplier").getAsFloat());
                }

                if (combo.has("can_be_interrupt")) {
                    child.setCanBeInterrupt(combo.get("can_be_interrupt").getAsBoolean());
                }

                if (combo.has("stun_type")) {
                    child.setStunTypeModifier(StunType.valueOf(combo.get("stun_type").getAsString()));
                }

                if (combo.has("convert_time")) {
                    child.setConvertTime(combo.get("convert_time").getAsFloat());
                }

                if (combo.has("not_charge")) {
                    child.setNotCharge(combo.get("not_charge").getAsBoolean());
                }

                if (combo.has("set_phase")) {
                    child.setNewPhase(combo.get("set_phase").getAsInt());
                }

                if (combo.has("cooldown")) {
                    child.setCooldown(combo.get("cooldown").getAsInt());
                }

                //获取判断条件
                if (combo.has("conditions")) {
                    JsonArray conditionList = combo.getAsJsonArray("conditions");
                    for (JsonElement conditionElement : conditionList) {
                        JsonObject condition = conditionElement.getAsJsonObject();
                        CompoundTag tag = TagParser.parseTag(condition.toString());
                        if(tag.getString("predicate").isEmpty()){
                            continue;
                        }
                        Condition<? extends LivingEntityPatch<?>> predicate = MobPatchReloadListener.deserializeBehaviorPredicate(tag.getString("predicate"), tag);
                        child.addCondition(predicate);
                    }
                }

                //获取命令列表
                if (combo.has("time_command_list")) {
                    JsonArray commandList = combo.getAsJsonArray("time_command_list");
                    for (JsonElement commandElement : commandList) {
                        JsonObject command = commandElement.getAsJsonObject();
                        float time = command.get("time").getAsFloat();
                        String commandText = command.get("command").getAsString();
                        boolean executeAtTarget = command.get("execute_at_target").getAsBoolean();
                        child.addTimeEvent(TimeStampedEvent.createTimeCommandEvent(time, commandText, executeAtTarget));
                    }
                }
                if (combo.has("hit_command_list")) {
                    JsonArray commandList = combo.getAsJsonArray("hit_command_list");
                    for (JsonElement commandElement : commandList) {
                        JsonObject command = commandElement.getAsJsonObject();
                        String commandText = command.get("command").getAsString();
                        boolean executeAtTarget = command.get("execute_at_target").getAsBoolean();
                        child.addHitEvent(BiEvent.createBiCommandEvent(commandText, executeAtTarget));
                    }
                }
                if (combo.has("hurt_command_list")) {
                    JsonArray commandList = combo.getAsJsonArray("hurt_command_list");
                    for (JsonElement commandElement : commandList) {
                        JsonObject command = commandElement.getAsJsonObject();
                        String commandText = command.get("command").getAsString();
                        boolean executeAtTarget = command.get("execute_at_target").getAsBoolean();
                        child.addHurtEvent(BiEvent.createBiCommandEvent(commandText, executeAtTarget));
                    }
                }
                if (combo.has("dodge_success_command_list")) {
                    JsonArray commandList = combo.getAsJsonArray("dodge_success_command_list");
                    for (JsonElement commandElement : commandList) {
                        JsonObject command = commandElement.getAsJsonObject();
                        String commandText = command.get("command").getAsString();
                        boolean executeAtTarget = command.get("execute_at_target").getAsBoolean();
                        child.addDodgeSuccessEvent(BiEvent.createBiCommandEvent(commandText, executeAtTarget));
                    }
                }

                //有优先级代表是属于ConditionAnimations列表的
                if(combo.has("priority")){
                    child.setPriority(combo.get("priority").getAsInt());
                    parent.addConditionAnimation(child);
                }
            }

            //递归构造下一个按键
            if (combo.has("combos")) {
                invincible$deserializeCombos(child, combo.getAsJsonArray("combos"));
            }

            //绑定按键
            if(combo.has("key")){
                String key = combo.get("key").getAsString();
                ComboType keyType = ComboNode.ComboTypes.valueOf(key);
                parent.addChild(keyType, child);
            }
        }
    }

}
