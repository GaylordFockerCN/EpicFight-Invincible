package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.api.events.TimeStampedEvent;
import com.p1nero.invincible.conditions.*;
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
import yesman.epicfight.world.capabilities.entitypatch.Faction;
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
        ComboNode root = ComboNode.create();
        ComboNode Vertical_Slash = ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        ComboNode Thrust_Slash = ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        ComboNode Rising_Slash = ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        ComboNode Side_Slash_Right =
                ComboNode.createNode(() -> Animations.BIPED_STEP_BACKWARD)
                        .addCondition(new DownCondition())
                        .setPriority(3);
        ComboNode Side_Slash_Left =
                ComboNode.createNode(() -> Animations.BIPED_STEP_LEFT)
                        .addCondition(new LeftCondition())
                        .setPriority(2);
        ComboNode Side_Slash_Back =
                ComboNode.createNode(() -> Animations.BIPED_STEP_RIGHT)
                        .addCondition(new RightCondition())
                        .setPriority(1);
        ComboNode Side_Slash_FOR =
                ComboNode.createNode(() -> Animations.BIPED_STEP_FORWARD);

        ComboNode Side_Slash = ComboNode.create();
        Side_Slash.addConditionAnimation(Side_Slash_Right)
                .addConditionAnimation(Side_Slash_Left)
                .addConditionAnimation(Side_Slash_Back)
                .addConditionAnimation(Side_Slash_FOR);
        //
        ComboNode Spirit_Slash1 = ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        ComboNode Spirit_Slash2 = ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        ComboNode Spirit_Slash3 = ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        ComboNode Spirit_Round_Slash =
                ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        //
        //        // 见！切！
        ComboNode Foresight_Slash = ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        //        // 气刃突刺
        ComboNode Spirit_Thrust = ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        //        // 登！龙！
        //        ComboNode Spirit_Helm_Breaker =
        // ComboNode.createNode(()->Animations.TAISWORD_SPIRIT_HELM_BREAKER);
        //        // 小居合
        ComboNode Ju = ComboNode.createNode(() -> Animations.SWORD_AUTO1);

        ComboNode Ju_Slash = ComboNode.createNode(() -> Animations.SWORD_AUTO1);
        //        // 大！居！合！
        //        ComboNode Ju_Spirit_Slash =
        // ComboNode.createNode(()->Animations.TAISWORD_JU_SPIRIT_SLASH);

        root.key1(Vertical_Slash);
        Vertical_Slash.key1_2(Side_Slash);
        root.key2(Thrust_Slash);
        root.key3(Spirit_Slash1);
        root.key1_2(Side_Slash);
        root.key1_3(Spirit_Thrust);
        root.key2_3(Foresight_Slash);
        root.key3_4(Ju);
        root.key1_4(Ju_Slash);

        Ju.key1(Ju_Slash);

        Vertical_Slash.key1(Thrust_Slash).key3_4(Ju).key2_3(Foresight_Slash);

        Thrust_Slash.key1(Rising_Slash).key3_4(Ju).key2_3(Foresight_Slash);

        Rising_Slash.key1(Vertical_Slash).key3_4(Ju).key2_3(Foresight_Slash);

        Foresight_Slash.key3_4(Ju);
        Spirit_Slash1.key3(Spirit_Slash2);
        Spirit_Slash2.key3(Spirit_Slash3);
        Spirit_Slash3.key3(Spirit_Round_Slash);

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
