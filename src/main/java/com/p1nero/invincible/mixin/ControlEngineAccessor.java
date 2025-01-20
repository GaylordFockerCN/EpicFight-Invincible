package com.p1nero.invincible.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.skill.SkillSlot;

/**
 * 预存输入用
 */
@Mixin(value = ControllEngine.class, remap = false)
public interface ControlEngineAccessor {

    @Accessor("reserveCounter")
    void setReserveCounter(int counter);

    @Accessor("reservedKey")
    void setReservedKey(KeyMapping key);

    @Accessor("reservedOrChargingSkillSlot")
    void setReservedOrChargingSkillSlot(SkillSlot slot);

}
