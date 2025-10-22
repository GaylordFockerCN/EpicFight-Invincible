package com.p1nero.invincible.conditions;

import yesman.epicfight.data.conditions.entity.TargetInPov;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class InTargetPovCondition extends TargetInPov {

    @Override
    public boolean predicate(LivingEntityPatch<?> entityPatch) {
        LivingEntityPatch<?> livingEntityPatch = EpicFightCapabilities.getEntityPatch(entityPatch.getTarget(), LivingEntityPatch.class);
        return livingEntityPatch != null && super.predicate(livingEntityPatch);
    }

    public static class InTargetPovHorizontal extends TargetInPovHorizontal {
        @Override
        public boolean predicate(LivingEntityPatch<?> entityPatch) {
            LivingEntityPatch<?> livingEntityPatch = EpicFightCapabilities.getEntityPatch(entityPatch.getTarget(), LivingEntityPatch.class);
            return livingEntityPatch != null && super.predicate(livingEntityPatch);
        }
    }
}
