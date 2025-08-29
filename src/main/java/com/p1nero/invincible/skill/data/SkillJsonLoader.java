package com.p1nero.invincible.skill.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.skill.SimpleCustomInnateSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Unique;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.ArrayList;
import java.util.List;

public class SkillJsonLoader {
    public static void loadSkill(CompoundTag data) throws CommandSyntaxException {
        loadSkill(JsonParser.parseString(data.toString()).getAsJsonObject());
    }

    public static SimpleCustomInnateSkill.Builder loadSkill(JsonObject skill) throws CommandSyntaxException {
        boolean drawSkillIcon = false;
        ResourceLocation resourceLocation = null;
        if (skill.has("drawSkillIcon")) {
            drawSkillIcon = skill.get("drawSkillIcon").getAsBoolean();
        }
        if(skill.has("skillTextureLocation")) {
            resourceLocation = ResourceLocation.parse(skill.get("skillTextureLocation").getAsString());
        }
        ComboNode root = ComboNode.create();
        deserializeSkill(root, skill);

        List<String> tipList = new ArrayList<>();
        if (skill.has("descriptions")){
            for(JsonElement element : skill.get("descriptions").getAsJsonArray()){
                tipList.add(element.getAsString());
            }
        }

        return SimpleCustomInnateSkill.createCustomInnateSkill(SimpleCustomInnateSkill::new)
                .setShouldDrawGui(drawSkillIcon)
                .setCombo(root)
                .addToolTipOnItem(tipList)
                .setSkillTextureLocation(resourceLocation);
    }

    /**
     * 解析连击树
     */
    @Unique
    public static void deserializeSkill(ComboNode node, JsonObject combo) throws CommandSyntaxException {

        String animation = combo.get("animation").getAsString();
        node.setAnimationAccessorSupplier(() -> AnimationManager.byKey(animation));
        if (combo.has("speed_multiplier")) {
            node.setPlaySpeed(combo.get("speed_multiplier").getAsFloat());
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
            node.setDamageMultiplier(new ValueModifier.Unified(adder, multiplier, setter));
        }

        if (combo.has("hurt_damage_multiplier")) {
            node.setHurtDamageMultiplier(combo.get("hurt_damage_multiplier").getAsFloat());
        }

        if (combo.has("armor_negation")) {
            node.setArmorNegation(combo.get("armor_negation").getAsFloat());
        }

        if (combo.has("impact_multiplier")) {
            node.setImpactMultiplier(combo.get("impact_multiplier").getAsFloat());
        }

        if (combo.has("can_be_interrupt")) {
            node.setCanBeInterrupt(combo.get("can_be_interrupt").getAsBoolean());
        }

        if (combo.has("stun_type")) {
            node.setStunTypeModifier(StunType.valueOf(combo.get("stun_type").getAsString()));
        }

        if (combo.has("convert_time")) {
            node.setConvertTime(combo.get("convert_time").getAsFloat());
        }

        if (combo.has("not_charge")) {
            node.setNotCharge(combo.get("not_charge").getAsBoolean());
        }

        if (combo.has("set_phase")) {
            node.setNewPhase(combo.get("set_phase").getAsInt());
        }

        if (combo.has("cooldown")) {
            node.setCooldown(combo.get("cooldown").getAsInt());
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
                node.addCondition(predicate);
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
                node.addTimeEvent(TimeStampedEvent.createTimeCommandEvent(time, commandText, executeAtTarget));
            }
        }
        if (combo.has("hit_command_list")) {
            JsonArray commandList = combo.getAsJsonArray("hit_command_list");
            for (JsonElement commandElement : commandList) {
                JsonObject command = commandElement.getAsJsonObject();
                String commandText = command.get("command").getAsString();
                boolean executeAtTarget = command.get("execute_at_target").getAsBoolean();
                node.addHitEvent(BiEvent.createBiCommandEvent(commandText, executeAtTarget));
            }
        }
        if (combo.has("hurt_command_list")) {
            JsonArray commandList = combo.getAsJsonArray("hurt_command_list");
            for (JsonElement commandElement : commandList) {
                JsonObject command = commandElement.getAsJsonObject();
                String commandText = command.get("command").getAsString();
                boolean executeAtTarget = command.get("execute_at_target").getAsBoolean();
                node.addHurtEvent(BiEvent.createBiCommandEvent(commandText, executeAtTarget));
            }
        }
        if (combo.has("dodge_success_command_list")) {
            JsonArray commandList = combo.getAsJsonArray("dodge_success_command_list");
            for (JsonElement commandElement : commandList) {
                JsonObject command = commandElement.getAsJsonObject();
                String commandText = command.get("command").getAsString();
                boolean executeAtTarget = command.get("execute_at_target").getAsBoolean();
                node.addDodgeSuccessEvent(BiEvent.createBiCommandEvent(commandText, executeAtTarget));
            }
        }


    }


}
