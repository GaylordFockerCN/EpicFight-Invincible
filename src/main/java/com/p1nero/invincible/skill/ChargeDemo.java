package com.p1nero.invincible.skill;

import com.google.common.collect.Lists;
import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.api.combo.ComboType;
import com.p1nero.invincible.gameassets.combos.ComboDemo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;

public class ChargeDemo extends ComboBasicAttack{
    public ChargeDemo(Builder builder) {
        super(builder);
    }

    /**
     * 判断当前是否是对应节点并播放蓄力动画
     */
    @Override
    public void onPress(SkillContainer container, ServerPlayerPatch serverPlayerPatch, ComboType comboType) {
        if(this.getCurrentNode(container) == ComboDemo.basicAttack && comboType == ComboNode.ComboTypes.KEY_2) {
            container.getExecutor().playAnimationSynchronized(Animations.BIPED_DEMOLITION_LEAP_CHARGING, 0.1F);
        }
    }

    @Override
    public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
        List<Component> list = Lists.newArrayList();
        list.add(Component.translatable("tips.invincible.charge"));
        return list;
    }
}
