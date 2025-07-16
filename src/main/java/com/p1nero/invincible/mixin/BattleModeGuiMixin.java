package com.p1nero.invincible.mixin;

import com.mojang.blaze3d.platform.Window;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import com.p1nero.invincible.skill.ComboBasicAttack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlots;

@Mixin(value = BattleModeGui.class)
public abstract class BattleModeGuiMixin {

    @Shadow(remap = false) public abstract Font getFont();

    /**
     * 取消绘制技能图标
     */
    @Inject(method = "renderWeaponInnateSkill", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void invincible$modifyTexture(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci){
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if(playerPatch != null) {
            SkillContainer container = playerPatch.getSkill(SkillSlots.WEAPON_INNATE);
            if(container.getSkill() instanceof ComboBasicAttack comboBasicAttack && !comboBasicAttack.shouldDraw(container)){
                ci.cancel();
            }
        }
    }

    /**
     * 画冷却
     */
    @Inject(method = "renderWeaponInnateSkill", at = @At(value = "TAIL"))
    private void invincible$drawCooldown(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci){
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if(playerPatch != null) {
            SkillDataManager manager = playerPatch.getSkill(SkillSlots.WEAPON_INNATE).getDataManager();
            if(!manager.hasData(InvincibleSkillDataKeys.COOLDOWN)){
                return;
            }
            int cooldown = manager.getDataValue(InvincibleSkillDataKeys.COOLDOWN);
            if(cooldown > 0){
                Vec2i pos = ClientConfig.getWeaponInnatePosition();
                String s = String.format("%.1fs", cooldown / 20.0);
                int stringWidth = (this.getFont().width(s) - 6) / 3;
                guiGraphics.drawString(this.getFont(), s, pos.x - stringWidth, pos.y + 22, 16777215, true);
            }
        }
    }

}