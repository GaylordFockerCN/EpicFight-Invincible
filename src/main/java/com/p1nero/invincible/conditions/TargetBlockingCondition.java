package com.p1nero.invincible.conditions;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import com.p1nero.invincible.mixin.AdvancedCustomHumanoidMobPatchAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.ModList;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

/**
 * 仅在坚不可摧（indestructible）加载时可以使用
 */
public class TargetBlockingCondition implements Condition<ServerPlayerPatch> {

    private boolean isParry;

    public TargetBlockingCondition(){

    }

    public TargetBlockingCondition(boolean isParry){
        this.isParry = isParry;
    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        if (!compoundTag.contains("is_parry")) {
            throw new IllegalArgumentException("custom target blocking condition error: is_parry not specified!");
        }  else {
            this.isParry = compoundTag.getBoolean("is_parry");
            return this;
        }
    }

    @Override
    public CompoundTag serializePredicate() {
        return new CompoundTag();
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        if(!ModList.get().isLoaded("indestructible") || serverPlayerPatch.getTarget() == null){
            return false;
        }
        AdvancedCustomHumanoidMobPatch<?> patch = EpicFightCapabilities.getEntityPatch(serverPlayerPatch.getTarget(), AdvancedCustomHumanoidMobPatch.class);
        if(patch != null){
            if(isParry){
                return ((AdvancedCustomHumanoidMobPatchAccessor) patch).isParry();
            }
            return patch.isBlocking();
        }
        return false;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
