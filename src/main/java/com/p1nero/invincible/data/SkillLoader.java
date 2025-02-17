package com.p1nero.invincible.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.skill.ComboBasicAttack;
import com.p1nero.invincible.skill.api.ComboNode;
import com.p1nero.invincible.skill.api.ComboType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Unique;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

@SuppressWarnings("UnstableApiUsage")
public class SkillLoader {
    public static void loadSkill(CompoundTag data) throws CommandSyntaxException {
        loadSkill(JsonParser.parseString(data.toString()).getAsJsonObject());
    }

    public static void loadSkill(JsonObject weapon) throws CommandSyntaxException {
        ForgeRegistry<Skill> registry = (ForgeRegistry<Skill>) SkillManager.getSkillRegistry();
        InvincibleMod.LOGGER.warn("unfreezing Skill Registry.");
        registry.unfreeze();
        String modId = weapon.get("mod_id").getAsString();
        String name = weapon.get("name").getAsString();
        //防止读到模板以及避免无数据包情况下服务端进不去
        if (modId.isEmpty() || (FMLEnvironment.dist.isDedicatedServer() && name.equals("datapack_demo"))) {
            return;
        }
        boolean drawSkillIcon = false;
        if (weapon.has("drawSkillIcon")) {
            drawSkillIcon = weapon.get("drawSkillIcon").getAsBoolean();
        }

        JsonArray combos = weapon.getAsJsonArray("combos");

        ComboNode root = ComboNode.create();
        deserializeCombos(root, combos);

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
        registry.freeze();
        InvincibleMod.LOGGER.warn("freezing Skill Registry.");
    }

    /**
     * 解析连击树
     */
    @Unique
    public static void deserializeCombos(ComboNode parent, JsonArray combos) throws CommandSyntaxException {

        for (JsonElement comboElement : combos) {
            JsonObject combo = comboElement.getAsJsonObject();
            ComboNode child = ComboNode.create();

            //有condition_animations说明为特殊类型，要借递归进行特殊处理
            if(combo.has("condition_animations")){
                JsonArray conditionAnimationsListList = combo.getAsJsonArray("condition_animations");
                //把自己传进去，解析以保存各种可能的动画参数
                deserializeCombos(child, conditionAnimationsListList);
            } else {
                String animation = combo.get("animation").getAsString();
//                child.setAnimationProvider(() -> AnimationManager.getInstance().byKeyOrThrow(animation));//FIXME 暂时无替代

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

                if (combo.has("armor_negation")) {
                    child.setArmorNegation(combo.get("armor_negation").getAsFloat());
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
                deserializeCombos(child, combo.getAsJsonArray("combos"));
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
