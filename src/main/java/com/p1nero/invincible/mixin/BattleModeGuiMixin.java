package com.p1nero.invincible.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ConfigurationIngame;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;

@Mixin(value = BattleModeGui.class)
public class BattleModeGuiMixin {

    @Shadow(remap = false) public Font font;

    @Shadow(remap = false) @Final private ConfigurationIngame config;

    /**
     * 取消绘制技能图标
     */
    @Inject(method = "drawWeaponInnateIcon", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void invincible$modifyTexture(LocalPlayerPatch playerpatch, SkillContainer container, PoseStack matStack, float partialTicks, CallbackInfo ci){
        if(container.getSkill() instanceof ComboBasicAttack comboBasicAttack && !comboBasicAttack.isShouldDrawGui()){
            ci.cancel();
        }
    }

    /**
     * 画冷却
     */
    @Inject(method = "drawWeaponInnateIcon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void invincible$drawCooldown(LocalPlayerPatch playerPatch, SkillContainer container, PoseStack matStack, float partialTicks, CallbackInfo ci){
        SkillDataManager manager = container.getDataManager();
        int cooldown = manager.hasData(ComboBasicAttack.COOLDOWN_TIMER) ? manager.getDataValue(ComboBasicAttack.COOLDOWN_TIMER) : 0;
        if(cooldown > 0){
            Window sr = Minecraft.getInstance().getWindow();
            int width = sr.getGuiScaledWidth();
            int height = sr.getGuiScaledHeight();
            Vec2i pos = this.config.getWeaponInnatePosition(width, height);
            String s = String.format("%.1fs", cooldown / 20.0);
            int stringWidth = (this.font.width(s) - 6) / 3;
            this.font.drawShadow(matStack, s, (float)(pos.x - stringWidth), (float)(pos.y + 22), 16777215);
        }
    }

}