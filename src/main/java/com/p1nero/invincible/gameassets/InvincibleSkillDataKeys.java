package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.SkillDataKey;

public class InvincibleSkillDataKeys {
    public static final DeferredRegister<SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(EpicFightRegistries.SKILL_DATA_KEY, InvincibleMod.MOD_ID);

    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> LEFT = DATA_KEYS.register("left", () ->
            SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, false, ComboBasicAttack.class));//a按键是否按下
    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> RIGHT = DATA_KEYS.register("right", () ->
            SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, false, ComboBasicAttack.class));//a按键是否按下
    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> UP = DATA_KEYS.register("up", () ->
            SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, false, ComboBasicAttack.class));//a按键是否按下
    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> DOWN = DATA_KEYS.register("down", () ->
            SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, false, ComboBasicAttack.class));//a按键是否按下
    
    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> PARRY_TIMER = DATA_KEYS.register("parry_timer", () ->
            SkillDataKey.createSkillDataKey(ByteBufCodecs.INT, 0, false, ComboBasicAttack.class));//是否成功格挡计时器
    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> DODGE_SUCCESS_TIMER = DATA_KEYS.register("dodge_success_timer", () ->
            SkillDataKey.createSkillDataKey(ByteBufCodecs.INT, 0, false, ComboBasicAttack.class));//是否成功闪避计时器
    public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Integer>> COOLDOWN = DATA_KEYS.register("cooldown", () ->
            SkillDataKey.createSkillDataKey(ByteBufCodecs.INT, 0, false, ComboBasicAttack.class));//冷却计时器


}
