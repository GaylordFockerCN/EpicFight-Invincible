package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.conditions.CustomCondition;
import com.p1nero.invincible.conditions.JumpCondition;
import com.p1nero.invincible.conditions.SprintingCondition;
import com.p1nero.invincible.conditions.StackCondition;
import com.p1nero.invincible.skill.ComboBasicAttack;
import com.p1nero.invincible.skill.api.ComboNode;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册技能，然后在{@link InvincibleWeaponCapabilityPresets}中使用
 * 预设的Condition可以参考 {@link yesman.epicfight.data.conditions.EpicFightConditions} 和 {@link InvincibleConditions}
 */
@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InvincibleSkills {
    public static Skill COMBO_DEMO;
    public static final List<CompoundTag> NEW_SKILLS = new ArrayList<>();

    @SubscribeEvent
    public static void BuildSkills(SkillBuildEvent event) {
        SkillBuildEvent.ModRegistryWorker registryWorker = event.createRegistryWorker(InvincibleMod.MOD_ID);

        //Create a combo tree
        //建立连击树
        //I use epic fight Condition system. So you can use custom condition or mine or epic fight's
        //我使用史诗战斗的Condition系统，这意味着你可以自定义条件，也可以用我和史诗战斗给的预设
        ComboNode root = ComboNode.create();
        ComboNode a = ComboNode.createNode(() -> Animations.SWORD_AUTO1)
                .setPlaySpeed(0.5F)//测试变速 speed modify test
                .addTimeEvent(new TimeStampedEvent(0.12F, (entityPatch -> {
                    if (entityPatch.getOriginal() instanceof ServerPlayer serverPlayer) {
                        serverPlayer.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 10, 1, 1, 1, 1);
                    }
                })))
                .addTimeEvent(TimeStampedEvent.createTimeCommandEvent(0.22F, "summon minecraft:zombie", false));
        ComboNode aa = ComboNode.createNode(() -> Animations.SWORD_AUTO2);
        ComboNode ab = ComboNode.createNode(() -> Animations.LONGSWORD_AUTO2);
        ComboNode aaa = ComboNode.createNode(() -> Animations.SWORD_AUTO3);
        ComboNode aab = ComboNode.createNode(() -> Animations.LONGSWORD_AUTO3);
        //自定义条件
        ComboNode aaaa = ComboNode.createNode(() -> Animations.SWEEPING_EDGE).addCondition(new JumpCondition()).addCondition(new CustomCondition() {
            @Override
            public boolean predicate(LivingEntityPatch<?> entityPatch) {
                return true;
            }
        });
        ComboNode aaab = ComboNode.createNode(() -> Animations.BIPED_STEP_BACKWARD).addCondition(new JumpCondition());
        ComboNode b = ComboNode.create();
        //不同条件播不同动画
        b.addConditionAnimation(ComboNode.createNode(() -> Animations.LONGSWORD_AUTO1).addCondition(new SprintingCondition()).setPriority(2));
        b.addConditionAnimation(ComboNode.createNode(() -> Animations.BIPED_STEP_BACKWARD).addCondition(new JumpCondition()).setPriority(1));
        ComboNode bb = ComboNode.createNode(() -> Animations.LONGSWORD_AUTO2);
        ComboNode bbb = ComboNode.createNode(() -> Animations.LONGSWORD_AUTO3);
        ComboNode a_b = ComboNode.createNode(() -> Animations.UCHIGATANA_SHEATHING_DASH).addCondition(new StackCondition(1, 2)).setNotCharge(true);
        a.key1(aa);
        a.key2(ab);
        aa.key1(aaa);
        aa.key2(aab);
        aaa.key1(aaaa);
        aaa.key2(aaab);
        root.key1(a);
        b.key2(bb);
        bb.key2(bbb);
        root.key2(b);
        root.key1_2(a_b);
        root.keyWeaponInnate(() -> Animations.BIPED_STEP_LEFT);//测试特定按键
        COMBO_DEMO = registryWorker.build("combo_demo", ComboBasicAttack::new, ComboBasicAttack.createComboBasicAttack().setCombo(root).setShouldDrawGui(false));

        //You can also create the tree like this:
        //你也可以这样构建：
        ComboNode root2 = ComboNode.create()
                .key1(ComboNode.createNode(() -> Animations.SWORD_AUTO1)
                        .key1(ComboNode.createNode(() -> Animations.SWORD_AUTO2)
                                .key1(() -> Animations.SWORD_AUTO3).addCondition(new JumpCondition())
                                .key2(() -> Animations.LONGSWORD_AUTO3))
                        .key2(() -> Animations.SWORD_AUTO2))
                .key2(ComboNode.createNode(() -> Animations.LONGSWORD_AUTO1));

    }

}
