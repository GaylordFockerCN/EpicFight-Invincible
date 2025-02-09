package com.p1nero.invincible.conditions;

import com.nameless.indestructible.world.capability.AdvancedCustomHumanoidMobPatch;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.fml.ModList;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class BlockingCondition implements Condition<ServerPlayerPatch> {

    private final boolean isTarget;
    public BlockingCondition(boolean isTarget){
        this.isTarget = isTarget;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        if(isTarget){
            if(serverPlayerPatch.getTarget() == null){
                return false;
            }
            if(serverPlayerPatch.getTarget() instanceof ServerPlayer){
                return check(EpicFightCapabilities.getEntityPatch(serverPlayerPatch.getTarget(), ServerPlayerPatch.class));
            }
            if(ModList.get().isLoaded("indestructible")){
                return EpicFightCapabilities.getEntityPatch(serverPlayerPatch.getTarget(), AdvancedCustomHumanoidMobPatch.class).isBlocking();
            } else {
                throw new IllegalStateException("try to use TargetGuardBreakCondition without indestructible!");
            }
        }
        return check(serverPlayerPatch);
    }

    public static boolean check(ServerPlayerPatch serverPlayerPatch){
        Skill guard = serverPlayerPatch.getSkill(SkillSlots.GUARD).getSkill();
        CapabilityItem capabilityItem = EpicFightCapabilities.getItemStackCapability(serverPlayerPatch.getOriginal().getMainHandItem());
        return serverPlayerPatch.getOriginal().isUsingItem() && guard != null
                && capabilityItem.getUseAnimation(serverPlayerPatch) == UseAnim.BLOCK
                    && guard.isExecutableState(serverPlayerPatch);
    }

}
