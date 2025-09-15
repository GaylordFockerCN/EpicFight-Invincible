package com.p1nero.invincible.api.animations;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

/**
 * 适合给弓之类的，碰撞箱给大，可自动转向且最近的Target
 */
public class ScanAttackAnimation extends AttackAnimation {
    public ScanAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, Joint colliderJoint, String path, AssetAccessor<? extends Armature> armature) {
        super(convertTime, antic, preDelay, contact, recovery, hand, collider, colliderJoint, path, armature);
    }

    public ScanAttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, Phase... phases) {
        super(convertTime, path, armature, phases);
    }

    public ScanAttackAnimation(float transitionTime, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature, Phase... phases) {
        super(transitionTime, accessor, armature, phases);
    }

    public ScanAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, antic, preDelay, contact, recovery, collider, colliderJoint, accessor, armature);
    }

    public ScanAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, Joint colliderJoint, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, antic, preDelay, contact, recovery, hand, collider, colliderJoint, accessor, armature);
    }

    @Override
    public void begin(LivingEntityPatch<?> entitypatch) {
        entitypatch.removeHurtEntities();
        super.begin(entitypatch);
    }

    @Override
    protected Vec3 getCoordVector(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> dynamicAnimation) {
        Vec3 vec3 = super.getCoordVector(entitypatch, dynamicAnimation);
        if (entitypatch.shouldBlockMoving() && this.getProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE).orElse(false)) {
            vec3 = vec3.scale(0.0F);
        }

        return vec3;
    }

    @Override
    protected void attackTick(LivingEntityPatch<?> entityPatch, AssetAccessor<? extends DynamicAnimation> animation) {
        AnimationPlayer player = entityPatch.getAnimator().getPlayerFor(this.getAccessor());
        if(player == null) {
            return;
        }
        float prevElapsedTime = player.getPrevElapsedTime();
        float elapsedTime = player.getElapsedTime();
        EntityState prevState = animation.get().getState(entityPatch, prevElapsedTime);
        EntityState state = animation.get().getState(entityPatch, elapsedTime);
        Phase phase = this.getPhaseByTime(animation.get().isLinkAnimation() ? 0.0F : elapsedTime);

        LivingEntity target = entityPatch.getTarget();
        if(target == null) {
            target = getNearestScannedTarget(entityPatch);
        }
        if (target != null && elapsedTime < phase.contact) {
            Vec3 playerPosition = entityPatch.getOriginal().position();
            Vec3 targetPosition = target.position();
            float yaw = (float) MathUtils.getYRotOfVector(targetPosition.subtract(playerPosition));
            entityPatch.setYRot(yaw);
        }

        if (prevState.attacking() || state.attacking() || prevState.getLevel() <= 2 && state.getLevel() > 2) {
            if (!prevState.attacking() || phase != this.getPhaseByTime(prevElapsedTime) && (state.attacking() || prevState.getLevel() <= 2 && state.getLevel() > 2)) {
                entityPatch.onStrike(this, phase.hand);
                entityPatch.removeHurtEntities();
            }

            this.searchNearestEntity(entityPatch, prevElapsedTime, elapsedTime, prevState, state, phase);
        }

    }

    @Nullable
    public static LivingEntity getTarget(LivingEntityPatch<?> entityPatch) {
        if(entityPatch.getTarget() != null) {
            return entityPatch.getTarget();
        }
        return getNearestScannedTarget(entityPatch);
    }

    @Nullable
    public static LivingEntity getNearestScannedTarget(LivingEntityPatch<?> entityPatch) {
        if(entityPatch.getCurrentlyAttackTriedEntities().isEmpty()) {
            return null;
        }
        Entity entity = entityPatch.getCurrentlyAttackTriedEntities().get(0);
        return entity instanceof LivingEntity living ? living : null;
    }

    protected void searchNearestEntity(LivingEntityPatch<?> entityPatch, float prevElapsedTime, float elapsedTime, EntityState prevState, EntityState state, Phase phase) {
        LivingEntity entity = entityPatch.getOriginal();
        float prevPoseTime = prevState.attacking() ? prevElapsedTime : phase.preDelay;
        float poseTime = state.attacking() ? elapsedTime : phase.contact;
        List<Entity> list = this.getPhaseByTime(elapsedTime).getCollidingEntities(entityPatch, this, prevPoseTime, poseTime, this.getPlaySpeed(entityPatch, this));
        if (!list.isEmpty()) {
            HitEntityList hitEntities = new HitEntityList(entityPatch, list, phase.getProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY).orElse(HitEntityList.Priority.DISTANCE));
            while(hitEntities.next()) {
                Entity target = hitEntities.getEntity();
                LivingEntity trueEntity = this.getTrueEntity(target);
                if (trueEntity != null && trueEntity.isAlive() && !entityPatch.getCurrentlyAttackTriedEntities().contains(trueEntity) && !entityPatch.isTargetInvulnerable(target) && (target instanceof LivingEntity || target instanceof PartEntity) && entity.hasLineOfSight(target)) {
                    entityPatch.getCurrentlyAttackTriedEntities().add(trueEntity);
                    entityPatch.getCurrentlyAttackTriedEntities().sort((e1, e2) -> Float.compare(e1.distanceTo(entity), e2.distanceTo(entity)));
                }
            }
        }

    }
}
