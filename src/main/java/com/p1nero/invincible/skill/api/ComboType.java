package com.p1nero.invincible.skill.api;

import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface ComboType extends ExtendableEnum {
    List<ComboType> SORTED_ALL_TYPES = new ArrayList<>();
    static void initList(){
        List<ComboType> typeList = new ArrayList<>(ComboType.ENUM_MANAGER.universalValues().stream().toList());
        typeList.sort(Comparator.comparingInt((comboType) -> -1 * comboType.getSubTypes().size()));//subType多的优先
        SORTED_ALL_TYPES.addAll(typeList);
    }
    ExtendableEnumManager<ComboType> ENUM_MANAGER = new ExtendableEnumManager<>("combo_type");
    List<ComboType> getSubTypes();
}
