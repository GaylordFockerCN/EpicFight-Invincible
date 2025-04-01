package com.p1nero.invincible.conditions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.UseAnim;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;

/**
 * 仅在坚不可摧（indestructible）加载时可以使用
 */
public class BlockingCondition implements Condition<ServerPlayerPatch> {

    public BlockingCondition() {

    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        return new CompoundTag();
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        SkillContainer guardSkill = serverPlayerPatch.getSkill(SkillSlots.GUARD);
        CapabilityItem itemCapability = serverPlayerPatch.getHoldingItemCapability((serverPlayerPatch.getOriginal().getUsedItemHand()));
        return itemCapability.getUseAnimation(serverPlayerPatch) == UseAnim.BLOCK && (serverPlayerPatch.getOriginal().isUsingItem() && guardSkill.getSkill() != null && guardSkill.getSkill().isExecutableState(serverPlayerPatch));

    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
