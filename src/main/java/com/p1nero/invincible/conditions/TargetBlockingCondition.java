package com.p1nero.invincible.conditions;

//import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
//import com.p1nero.invincible.mixin.AdvancedCustomHumanoidMobPatchAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.UseAnim;
import net.neoforged.fml.ModList;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
//import yesman.epicfight.world.capabilities.EpicFightCapabilities;
//import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
//import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;

/**
 * 仅在坚不可摧（indestructible）加载时可以使用
 */
public class TargetBlockingCondition implements Condition<ServerPlayerPatch> {

    public TargetBlockingCondition() {

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
        if (serverPlayerPatch.getTarget() instanceof ServerPlayer serverPlayer) {
            SkillContainer guardSkill = EpicFightCapabilities.getEntityPatch(serverPlayer, ServerPlayerPatch.class).getSkill(SkillSlots.GUARD);
            CapabilityItem itemCapability = serverPlayerPatch.getHoldingItemCapability((serverPlayer.getUsedItemHand()));
            return itemCapability.getUseAnimation(serverPlayerPatch) == UseAnim.BLOCK && (serverPlayer.isUsingItem() && guardSkill.getSkill() != null && guardSkill.getSkill().isExecutableState(serverPlayerPatch));
        }
        if (!ModList.get().isLoaded("indestructible") || serverPlayerPatch.getTarget() == null) {
            return false;
        }
//        AdvancedCustomHumanoidMobPatch<?> patch = EpicFightCapabilities.getEntityPatch(serverPlayerPatch.getTarget(), AdvancedCustomHumanoidMobPatch.class);
//        if (patch != null) {
//            return patch.isBlocking();
//        }
        return false;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
