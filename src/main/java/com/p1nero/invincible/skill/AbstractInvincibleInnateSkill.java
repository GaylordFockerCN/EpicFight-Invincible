package com.p1nero.invincible.skill;

import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.capability.InvinciblePlayer;
import com.p1nero.invincible.gameassets.InvincibleSkillDataKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

public class AbstractInvincibleInnateSkill extends Skill {

    public AbstractInvincibleInnateSkill(SkillBuilder<? extends Skill> builder) {
        super(builder);
    }

    /**
     * 根据预存来初始化玩家信息
     */
    protected void initPlayer(SkillContainer container, InvinciblePlayer invinciblePlayer, ComboNode dataNode) {
        invinciblePlayer.setCurrentDataNode(dataNode);
        if (dataNode.getCooldown() > 0) {
            container.getDataManager().setDataSync(InvincibleSkillDataKeys.COOLDOWN.get(), dataNode.getCooldown());
            invinciblePlayer.setItemCooldown(container.getExecutor().getOriginal().getMainHandItem(), dataNode.getCooldown());
        }
    }

}
