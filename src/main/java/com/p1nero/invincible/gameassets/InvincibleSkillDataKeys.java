package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillDataKey;

public class InvincibleSkillDataKeys {
    public static final DeferredRegister<SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(new ResourceLocation(EpicFightMod.MODID, "skill_data_keys"), InvincibleMod.MOD_ID);
}
