package com.p1nero.invincible.api.skill;

import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

import java.util.List;

public interface ComboType extends ExtendableEnum {
    ExtendableEnumManager<ComboType> ENUM_MANAGER = new ExtendableEnumManager<>("combo_type");
    List<ComboType> getSubTypes();
}
