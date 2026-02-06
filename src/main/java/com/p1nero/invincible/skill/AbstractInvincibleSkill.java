package com.p1nero.invincible.skill;

import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.attachment.InvinciblePlayer;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

public class AbstractInvincibleSkill extends Skill {

    public AbstractInvincibleSkill(SkillBuilder<?> builder) {
        super(builder);
    }

    /**
     * 根据预存来初始化玩家信息
     */
    protected void initPlayer(SkillContainer container, InvinciblePlayer invinciblePlayer, ComboNode dataNode) {
        invinciblePlayer.setCurrentDataNode(dataNode);
        if (dataNode.getCooldown() > 0 && !container.getExecutor().isLogicalClient()) {
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.COOLDOWN, dataNode.getCooldown());
            invinciblePlayer.setItemCooldown(container.getExecutor().getOriginal().getMainHandItem(), dataNode.getCooldown());
        }
    }

}
