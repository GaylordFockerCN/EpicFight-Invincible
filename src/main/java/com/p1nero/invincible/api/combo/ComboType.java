package com.p1nero.invincible.api.combo;

import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

import java.util.List;

public interface ComboType extends ExtensibleEnum {
    ExtensibleEnumManager<ComboType> ENUM_MANAGER = new ExtensibleEnumManager<>("combo_type");
    List<ComboType> getSubTypes();
}