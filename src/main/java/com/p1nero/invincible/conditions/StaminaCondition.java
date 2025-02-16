package com.p1nero.invincible.conditions;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * 检测耐力
 * 检测敌人时如果敌方非玩家则必须为坚不可摧实体
 */
public class StaminaCondition implements Condition<ServerPlayerPatch>{
    private final boolean isTarget, isLarger;
    private final float ratio;

    public StaminaCondition(boolean isTarget, int ratio, boolean isLarger) {
        this.isTarget = isTarget;
        this.ratio = ratio;
        this.isLarger = isLarger;
    }


    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        if(isTarget){
            if(serverPlayerPatch.getTarget() == null || !ModList.get().isLoaded("indestructible")){
                return false;
            }
            if(serverPlayerPatch.getTarget() instanceof ServerPlayer serverPlayer){
                return checkPlayer(EpicFightCapabilities.getEntityPatch(serverPlayer, ServerPlayerPatch.class));
            }
            AdvancedCustomHumanoidMobPatch<?> targetPatch = EpicFightCapabilities.getEntityPatch(serverPlayerPatch.getTarget(), AdvancedCustomHumanoidMobPatch.class);
            return targetPatch != null && isLarger == targetPatch.getStamina() / targetPatch.getMaxStamina() > ratio;

        } else {
            return checkPlayer(serverPlayerPatch);
        }
    }

    public boolean checkPlayer(ServerPlayerPatch serverPlayerPatch){
        return isLarger == serverPlayerPatch.getStamina() / serverPlayerPatch.getMaxStamina() > ratio;
    }

}
