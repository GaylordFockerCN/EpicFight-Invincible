package com.p1nero.invincible.animations;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
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
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

/**
 * 适合给弓之类的，碰撞箱给大，可自动转向且最近的设为Target
 */
public class ScanAttackAnimation extends AttackAnimation {
    public ScanAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, Joint colliderJoint, String path, AssetAccessor<? extends Armature> armature) {
        super(convertTime, antic, preDelay, contact, recovery, hand, collider, colliderJoint, path, armature);
    }

    public ScanAttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, AttackAnimation.Phase... phases) {
        super(convertTime, path, armature, phases);
    }

    public ScanAttackAnimation(float transitionTime, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature, AttackAnimation.Phase... phases) {
        super(transitionTime, accessor, armature, phases);
    }

    public ScanAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, antic, preDelay, contact, recovery, collider, colliderJoint, accessor, armature);
    }

    public ScanAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, Joint colliderJoint, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, antic, preDelay, contact, recovery, hand, collider, colliderJoint, accessor, armature);
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
        if(target == null && !entityPatch.getCurrentlyAttackTriedEntities().isEmpty()) {
            Entity nearest = entityPatch.getCurrentlyAttackTriedEntities().get(0);
            if(entityPatch instanceof ServerPlayerPatch serverPlayerPatch && nearest instanceof LivingEntity nearestLiving) {
                entityPatch.getCurrentlyAttackTriedEntities().sort((e1, e2) -> Float.compare(e1.distanceTo(entityPatch.getOriginal()), e2.distanceTo(entityPatch.getOriginal())));
                if(serverPlayerPatch.getTarget() == null) {
                    serverPlayerPatch.setAttackTarget(nearestLiving);
                }
            }
        }
        if (target != null && elapsedTime < phase.recovery) {
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

            this.hurtCollidingEntities(entityPatch, prevElapsedTime, elapsedTime, prevState, state, phase);
        }

    }

    @Override
    protected void hurtCollidingEntities(LivingEntityPatch<?> entityPatch, float prevElapsedTime, float elapsedTime, EntityState prevState, EntityState state, Phase phase) {
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
                    if(entityPatch instanceof ServerPlayerPatch serverPlayerPatch) {
                        entityPatch.getCurrentlyAttackTriedEntities().sort((e1, e2) -> Float.compare(e1.distanceTo(entity), e2.distanceTo(entity)));
                        Entity nearest = entityPatch.getCurrentlyAttackTriedEntities().get(0);
                        if(serverPlayerPatch.getTarget() == null && nearest instanceof LivingEntity living) {
                            serverPlayerPatch.setAttackTarget(living);
                        }
                    }
                }
            }
        }

    }
}
