package com.p1nero.invincible.data;

import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.RegisterEvent;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;

import static yesman.epicfight.api.data.reloader.SkillManager.SKILL_REGISTRY_KEY;

public class ComboSkillManager {
    public static void registerSkills(RegisterEvent event) {
        if (event.getRegistryKey().equals(SKILL_REGISTRY_KEY)) {
            final SkillBuildEvent skillBuildEvent = new SkillBuildEvent();
            ModLoader.get().postEvent(skillBuildEvent);
            event.register(SKILL_REGISTRY_KEY, (helper) -> {
                skillBuildEvent.getAllSkills().forEach((skill) -> {
                    helper.register(skill.getRegistryName(), skill);
                });
            });
        }
    }
}
