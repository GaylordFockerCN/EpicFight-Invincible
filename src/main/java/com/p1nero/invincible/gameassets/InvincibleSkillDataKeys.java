package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillDataKey;

public class InvincibleSkillDataKeys {
    public static final DeferredRegister<SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(new ResourceLocation(EpicFightMod.MODID, "skill_data_keys"), InvincibleMod.MOD_ID);

    public static final RegistryObject<SkillDataKey<Boolean>> LEFT = DATA_KEYS.register("left", () ->
            SkillDataKey.createSkillDataKey(PacketBufferCodec.BOOLEAN, false, false, ComboBasicAttack.class));//a按键是否按下
    public static final RegistryObject<SkillDataKey<Boolean>> RIGHT = DATA_KEYS.register("right", () ->
            SkillDataKey.createSkillDataKey(PacketBufferCodec.BOOLEAN, false, false, ComboBasicAttack.class));//a按键是否按下
    public static final RegistryObject<SkillDataKey<Boolean>> UP = DATA_KEYS.register("up", () ->
            SkillDataKey.createSkillDataKey(PacketBufferCodec.BOOLEAN, false, false, ComboBasicAttack.class));//a按键是否按下
    public static final RegistryObject<SkillDataKey<Boolean>> DOWN = DATA_KEYS.register("down", () ->
            SkillDataKey.createSkillDataKey(PacketBufferCodec.BOOLEAN, false, false, ComboBasicAttack.class));//a按键是否按下
    
    public static final RegistryObject<SkillDataKey<Integer>> PARRY_TIMER = DATA_KEYS.register("parry_timer", () ->
            SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, false, ComboBasicAttack.class));//是否成功格挡计时器

    public static final RegistryObject<SkillDataKey<Integer>> DODGE_SUCCESS_TIMER = DATA_KEYS.register("dodge_success_timer", () ->
            SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, false, ComboBasicAttack.class));//是否成功闪避计时器


}
