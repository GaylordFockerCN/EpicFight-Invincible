package com.p1nero.invincible.mixin;

import com.mojang.blaze3d.platform.Window;
import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import com.p1nero.invincible.capability.InvinciblePlayer;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.skill.SkillContainer;

@Mixin(value = BattleModeGui.class)
public class BattleModeGuiMixin {

    @Shadow(remap = false) public Font font;

    @Shadow(remap = false) @Final private EpicFightOptions config;

    /**
     * 取消绘制技能图标
     */
    @Inject(method = "drawWeaponInnateIcon", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void invincible$modifyTexture(LocalPlayerPatch playerPatch, SkillContainer container, GuiGraphics guiGraphics, float partialTicks, CallbackInfo ci){
        if(container.getSkill() instanceof ComboBasicAttack comboBasicAttack && !comboBasicAttack.shouldDraw(container)){
            ci.cancel();
        }
    }

    /**
     * 画冷却
     */
    @Inject(method = "drawWeaponInnateIcon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void invincible$drawCooldown(LocalPlayerPatch playerPatch, SkillContainer container, GuiGraphics guiGraphics, float partialTicks, CallbackInfo ci){
        if(!container.getDataManager().hasData(InvincibleSkillDataKeys.COOLDOWN.get())){
            return;
        }
        int cooldown = container.getDataManager().getDataValue(InvincibleSkillDataKeys.COOLDOWN.get());
        if(cooldown > 0){
            Window sr = Minecraft.getInstance().getWindow();
            int width = sr.getGuiScaledWidth();
            int height = sr.getGuiScaledHeight();
            Vec2i pos = this.config.getWeaponInnatePosition(width, height);
            String s = String.format("%.1fs", cooldown / 20.0);
            int stringWidth = (this.font.width(s) - 6) / 3;
            guiGraphics.drawString(font, s, pos.x - stringWidth, pos.y + 22, 16777215, true);
        }
    }

}