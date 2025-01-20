package com.p1nero.invincible.skill.api;

import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;
public interface ComboType extends ExtendableEnum {
    ExtendableEnumManager<ComboType> ENUM_MANAGER = new ExtendableEnumManager<>("combo_type");
    boolean canPressTogether();
}
