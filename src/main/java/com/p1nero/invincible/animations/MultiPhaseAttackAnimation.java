package com.p1nero.invincible.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.p1nero.invincible.capability.InvincibleEntities;
import com.p1nero.invincible.capability.InvincibleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import java.util.*;

public class MultiPhaseAttackAnimation extends AttackAnimation {

    public MultiPhaseAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, antic, preDelay, contact, recovery, collider, colliderJoint, accessor, armature);
    }

    public MultiPhaseAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, Joint colliderJoint, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, antic, preDelay, contact, recovery, hand, collider, colliderJoint, accessor, armature);
    }

    public MultiPhaseAttackAnimation(float transitionTime, AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor, AssetAccessor<? extends Armature> armature, Phase... phases) {
        super(transitionTime, accessor, armature, phases);
    }

    public MultiPhaseAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, Joint colliderJoint, String path, AssetAccessor<? extends Armature> armature) {
        super(convertTime, antic, preDelay, contact, recovery, hand, collider, colliderJoint, path, armature);
    }

    @Override
    public void begin(LivingEntityPatch<?> entityPatch) {
        super.begin(entityPatch);
        InvincibleEntities.getEntityCap(entityPatch.getOriginal()).removePhaseCache(phases);
    }

    @Override
    public void end(LivingEntityPatch<?> entityPatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
        super.end(entityPatch, nextAnimation, isEnd);
        InvincibleEntities.getEntityCap(entityPatch.getOriginal()).removePhaseCache(phases);
    }

    /**
     * 检查phase是否合法
     */
    protected boolean isPhaseValid(LivingEntityPatch<?> entityPatch, Phase phase){
        return true;
    }

    /**
     * 全部进行判断
     */
    @Override
    protected void attackTick(LivingEntityPatch<?> entityPatch, AssetAccessor<? extends DynamicAnimation> animation) {
        super.attackTick(entityPatch, animation);
        AnimationPlayer player = entityPatch.getAnimator().getPlayerFor(animation);
        float elapsedTime = player.getElapsedTime();
        float prevElapsedTime = player.getPrevElapsedTime();
        EntityState state = this.getState(entityPatch, elapsedTime);
        EntityState prevState = this.getState(entityPatch, prevElapsedTime);
        for(Phase phase : phases){
            if(!isPhaseValid(entityPatch, phase)){
                continue;
            }
            if (prevState.attacking() || state.attacking() || prevState.getLevel() < 2 && state.getLevel() > 2) {
                this.hurtCollidingEntities(entityPatch, prevElapsedTime, elapsedTime, prevState, state, phase);
            }
        }
    }

    protected void hurtCollidingEntities(LivingEntityPatch<?> entityPatch, float prevElapsedTime, float elapsedTime, EntityState prevState, EntityState state, Phase phase) {
        float prevPoseTime = prevState.attacking() ? prevElapsedTime : phase.preDelay;
        float poseTime = state.attacking() ? elapsedTime : phase.contact;
        List<Entity> list = phase.getCollidingEntities(entityPatch, this, prevPoseTime, poseTime, this.getPlaySpeed(entityPatch, this));

        if (!list.isEmpty()) {
            HitEntityList hitEntities = new HitEntityList(entityPatch, list, phase.getProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY).orElse(HitEntityList.Priority.DISTANCE));
            while (hitEntities.next()) {
                Entity hit = hitEntities.getEntity();
                LivingEntity trueEntity = this.getTrueEntity(hit);
                InvincibleEntity invincibleEntity = InvincibleEntities.getEntityCap(entityPatch.getOriginal());
                if (trueEntity != null && trueEntity.isAlive() && !invincibleEntity.getCurrentlyHurtEntities(phase).contains(trueEntity) && !trueEntity.is(entityPatch.getOriginal())) {
                    if (hit instanceof LivingEntity || hit instanceof PartEntity) {
                        EpicFightDamageSource source = this.getEpicFightDamageSource(entityPatch, hit, phase);
                        int prevInvulTime = hit.invulnerableTime;
                        hit.invulnerableTime = 0;

                        AttackResult attackResult = entityPatch.attack(source, hit, phase.hand);
                        hit.invulnerableTime = prevInvulTime;

                        if (attackResult.resultType.dealtDamage()) {
                            hit.level().playSound(null, hit.getX(), hit.getY(), hit.getZ(), this.getHitSound(entityPatch, phase), hit.getSoundSource(), 1.0F, 1.0F);
                            this.spawnHitParticle((ServerLevel)hit.level(), entityPatch, hit, phase);
                        }

                        invincibleEntity.getCurrentlyHurtEntities(phase).add(trueEntity);

                        if (attackResult.resultType.shouldCount()) {
                            entityPatch.getCurrentlyAttackTriedEntities().add(trueEntity);
                        }
                    }
                }
            }
        }
    }

    /**
     * 全部渲染
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderDebugging(PoseStack poseStack, MultiBufferSource buffer, LivingEntityPatch<?> entityPatch, float playbackTime, float partialTicks) {
        AnimationPlayer animPlayer = entityPatch.getAnimator().getPlayerFor(this.getAccessor());
        float prevElapsedTime = animPlayer.getPrevElapsedTime();
        float elapsedTime = animPlayer.getElapsedTime();
        for(Phase phase : phases){
            if(!isPhaseValid(entityPatch, phase)){
                continue;
            }
            Pair<Joint, Collider> colliderInfo;
            Collider collider;
            for(Iterator<JointColliderPair> iterator = Arrays.stream(phase.colliders).iterator(); iterator.hasNext(); collider.draw(poseStack, buffer, entityPatch, this, colliderInfo.getFirst(), prevElapsedTime, elapsedTime, partialTicks, this.getPlaySpeed(entityPatch, this))) {
                colliderInfo = iterator.next();
                collider = colliderInfo.getSecond();
            }
        }
    }

}
