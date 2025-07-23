package com.p1nero.invincible.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.particle.CustomModelParticle;
import yesman.epicfight.client.particle.EpicFightParticleRenderTypes;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class TransparentEntityAfterImageParticle extends CustomModelParticle<SkinnedMesh> {
    protected final EntitySnapshot<?> entitySnapshot;
    protected float alphaO;
    public TransparentEntityAfterImageParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, EntitySnapshot<?> entitySnapshot ) {
        super(level, x, y, z, xd, yd, zd, null);
        this.lifetime = 20;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alphaO = 0.3F;
        this.alpha = 0.3F;
        this.entitySnapshot = entitySnapshot;
        this.yawO = entitySnapshot.getYRot();
        this.yaw = entitySnapshot.getYRot();
    }

    @Override
    public void tick() {
        super.tick();
        this.alphaO = this.alpha;
        this.alpha = (float)(this.lifetime - this.age) / (float)this.lifetime * 0.8F;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        float alpha = (this.alphaO + (this.alpha - this.alphaO) * partialTicks);
        int lightColor = this.getLightColor(partialTicks);
        PoseStack poseStack = new PoseStack();
        this.setupPoseStack(poseStack, camera, partialTicks);
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        this.entitySnapshot.renderTextured(poseStack, buffers, EpicFightRenderTypes::entityAfterimageStencil, Mesh.DrawingFunction.POSITION_TEX, 0, 0.0F, 0.0F, 0.0F, 1.0F);
        this.entitySnapshot.renderItems(poseStack, buffers, EpicFightRenderTypes.itemAfterimageStencil(), Mesh.DrawingFunction.POSITION_TEX, lightColor, 1.0F);
        buffers.endLastBatch();
        this.entitySnapshot.renderTextured(poseStack, buffers, EpicFightRenderTypes::entityAfterimageTranslucent, Mesh.DrawingFunction.NEW_ENTITY, lightColor, this.rCol, this.gCol, this.bCol, alpha);
        this.entitySnapshot.renderItems(poseStack, buffers, EpicFightRenderTypes.itemAfterimageTranslucent(), Mesh.DrawingFunction.NEW_ENTITY, lightColor, alpha);
        buffers.endLastBatch();
        this.revert(poseStack);
    }


    protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTicks) {
        poseStack.pushPose();
        poseStack.mulPoseMatrix(RenderSystem.getModelViewStack().last().pose());
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
        Vec3 cameraPosition = camera.getPosition();
        float x = (float)(Mth.lerp(partialTicks, this.xo, this.x) - cameraPosition.x());
        float y = (float)(Mth.lerp(partialTicks, this.yo, this.y) - cameraPosition.y());
        float z = (float)(Mth.lerp(partialTicks, this.zo, this.z) - cameraPosition.z());
        poseStack.translate(x, y, z);
        Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
        float roll = Mth.rotLerp(partialTicks, this.oRoll, this.roll);
        float pitch = Mth.rotLerp(partialTicks, this.pitchO, this.pitch);
        float yaw = Mth.rotLerp(partialTicks, this.yawO, this.yaw);
        rotation.mul(QuaternionUtils.YP.rotationDegrees(180.0F - yaw));
        rotation.mul(QuaternionUtils.XP.rotationDegrees(pitch));
        rotation.mul(QuaternionUtils.ZP.rotationDegrees(roll));
        poseStack.mulPose(rotation);
        float scale = Mth.lerp(partialTicks, this.scaleO, this.scale);
        poseStack.translate(0.0F, this.entitySnapshot.getHeightHalf(), 0.0F);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, -this.entitySnapshot.getHeightHalf(), 0.0F);
    }

    protected void revert(PoseStack poseStack) {
        poseStack.popPose();
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return EpicFightParticleRenderTypes.ENTITY_PARTICLE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(@NotNull SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Entity entity = level.getEntity((int)Double.doubleToLongBits(xSpeed));
            LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            if (entityPatch != null && ClientEngine.getInstance().renderEngine.hasRendererFor(entityPatch.getOriginal())) {
                EntitySnapshot<?> entitySnapshot = entityPatch.captureEntitySnapshot();
                if (entitySnapshot != null){
                    return  new TransparentEntityAfterImageParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,entitySnapshot);
                }
            }

            return null;
        }
    }
}