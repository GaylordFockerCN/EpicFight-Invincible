package com.p1nero.invincible.gameassets.combos;

import com.p1nero.invincible.api.events.BiEvent;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.api.combo.ComboNode;
import com.p1nero.invincible.conditions.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.damagesource.StunType;

public class ComboDemo {
    public static ComboNode demo() {
        //我使用的是史诗战斗的Condition系统，这意味着你可以自定义条件，也可以用我和史诗战斗给的预设
        ComboNode root = ComboNode.create();
        ComboNode basicAttack = ComboNode.createNode(Animations.SWORD_AUTO1)//1a
                .setPlaySpeed(0.5F)//修改播放速度
                .setStunTypeModifier(StunType.KNOCKDOWN)//修改硬直类型
                .setDamageMultiplier(ValueModifier.multiplier(0.5F))//修改伤害
                .setCanBeInterrupt(false)//是否霸体
                .setImpactMultiplier(2.0F)//修改冲击
                .setConvertTime(0.15F)
                .setPriority(1)
                //自定义事件
                .addTimeEvent(new TimeStampedEvent(0.12F, (entityPatch -> {
                    if (entityPatch.getOriginal() instanceof ServerPlayer serverPlayer) {
                        serverPlayer.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 10, 1, 1, 1, 1);
                    }
                })));
        ComboNode jumpAttack = ComboNode.createNode(Animations.SWORD_AIR_SLASH).setPriority(3).addCondition(new JumpCondition());//修改了原版的跳跃攻击机制，以此补偿
        ComboNode dashAttack = ComboNode.createNode(Animations.SWORD_DASH).setPriority(2).addCondition(new SprintingCondition());//修改了原版的冲刺攻击机制，以此补偿
        ComboNode longPressAttack = ComboNode.createNode(Animations.SWORD_DASH).setPriority(4).addCondition(new PressedTimeCondition(20, Integer.MAX_VALUE));//长按的样例

        ComboNode a = ComboNode.create();
        a.addConditionNode(basicAttack).addConditionNode(jumpAttack).addConditionNode(dashAttack).addConditionNode(longPressAttack);
        root.key1(a);//初始态后按key1则根据不同条件来播放不同动画
        dashAttack.key1(a);//闭环
        jumpAttack.key1(a);//闭环

        ComboNode aa = ComboNode.createNode(Animations.SWORD_AUTO2);//2a
        aa.addHitEvent(new BiEvent((entityPatch, entity) -> {
            if (entityPatch.getOriginal() instanceof ServerPlayer serverPlayer) {
                serverPlayer.serverLevel().sendParticles(ParticleTypes.FLAME, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 10, 1, 1, 1, 1);
            }
        }));
        basicAttack.key1(aa);//只有播放普攻后按key1才能接2a

        ComboNode ab = ComboNode.createNode(Animations.LONGSWORD_AUTO2);
        basicAttack.key2(ab);//1a后按key2可变招

        ComboNode aaa = ComboNode.createNode(Animations.SWORD_AUTO3)//3a
                .addTimeEvent(new TimeStampedEvent(0.23F, (entityPatch -> entityPatch.playAnimationSynchronized(Animations.BIPED_STEP_BACKWARD, 0.15F))));//打断动画，即一个按键触发两次动画，但第二次动画无法执行事件。;
        aa.key1(aaa);
        aaa.key1(a);//闭环，增加手感

        ComboNode aab = ComboNode.createNode(Animations.LONGSWORD_AUTO3);
        aa.key2(aab);
        aab.key1(a);//闭环，增加手感

        ComboNode skill = ComboNode.createNode(Animations.UCHIGATANA_SHEATHING_DASH)
                .setCooldown(200)//进入冷却
                .addCondition(new StackCondition(1, 2))//检测Stack数量
                .addCondition(new CooldownCondition(false))//检测是否处于冷却状态
                .setNotCharge(true)//取消本次攻击的充能
                .addTimeEvent(TimeStampedEvent.createTimeCommandEvent(0.0F, "invincible consumeStack 1", false));
        skill.key1(a);
        root.keyWeaponInnate(skill);//常态才可以放
        a.keyWeaponInnate(skill);//随时可以按技能键释放技能
        aa.keyWeaponInnate(skill);
        aaa.keyWeaponInnate(skill);
        ab.keyWeaponInnate(skill);
        aab.keyWeaponInnate(skill);

        ComboNode l = ComboNode.createNode(Animations.BIPED_STEP_LEFT).addCondition(new LeftCondition());
        ComboNode r = ComboNode.createNode(Animations.BIPED_STEP_RIGHT).addCondition(new RightCondition());
        ComboNode f = ComboNode.createNode(Animations.BIPED_STEP_FORWARD).addCondition(new UpCondition());
        ComboNode ba = ComboNode.createNode(Animations.BIPED_STEP_BACKWARD).addCondition(new DownCondition());
        ComboNode dodge = ComboNode.create().addConditionNode(l).addConditionNode(r).addConditionNode(f).addConditionNode(ba);
        basicAttack.key1_2(dodge);//双键触发
        dodge.key1(a);

        //You can also create the tree like this:
        //你也可以这样构建：
        ComboNode root2 = ComboNode.create()
                .key1(ComboNode.createNode(Animations.SWORD_AUTO1)
                        .key1(ComboNode.createNode(Animations.SWORD_AUTO2)
                                .key1(Animations.SWORD_AUTO3).addCondition(new JumpCondition())
                                .key2(Animations.LONGSWORD_AUTO3))
                        .key2(Animations.SWORD_AUTO2))
                .key2(ComboNode.createNode(Animations.LONGSWORD_AUTO1));
        return root;
    }
}
