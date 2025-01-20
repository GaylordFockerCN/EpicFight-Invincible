package com.p1nero.invincible.gameassets;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.capability.TimeStampedEvent;
import com.p1nero.invincible.conditions.JumpCondition;
import com.p1nero.invincible.skill.ComboBasicAttack;
import com.p1nero.invincible.skill.api.ComboNode;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;

/**
 * 注册技能，然后在{@link InvincibleWeaponCapabilityPresets}中使用
 * 预设的Condition可以参考 {@link yesman.epicfight.data.conditions.EpicFightConditions} 和 {@link InvincibleConditions}
 */
@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InvincibleSkills {
    public static Skill COMBO_DEMO;

    @SubscribeEvent
    public static void BuildSkills(SkillBuildEvent event) {
        SkillBuildEvent.ModRegistryWorker registryWorker = event.createRegistryWorker(InvincibleMod.MOD_ID);

        //Create a combo tree
        //建立连击树
        //I use epic fight Condition system. So you can use custom condition or mine or epic fight's
        //我使用史诗战斗的Condition系统，这意味着你可以自定义条件，也可以用我和史诗战斗给的预设
        ComboNode root = ComboNode.createRoot();
        ComboNode a = ComboNode.createNode(() -> Animations.SWORD_AUTO1)
                .addEvent(new TimeStampedEvent(0.12F, (entityPatch -> {
                    if (entityPatch.getOriginal() instanceof ServerPlayer serverPlayer) {
                        serverPlayer.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 10, 1, 1, 1, 1);
                    }
                })))
                .addEvent(TimeStampedEvent.createTimeCommandEvent(0.22F, "summon minecraft:zombie", false));
        ComboNode aa = ComboNode.createNode(() -> Animations.SWORD_AUTO2);
        ComboNode ab = ComboNode.createNode(() -> Animations.LONGSWORD_AUTO2);
        ComboNode aaa = ComboNode.createNode(() -> Animations.SWORD_AUTO3);
        ComboNode aab = ComboNode.createNode(() -> Animations.LONGSWORD_AUTO3);
        ComboNode aaaa = ComboNode.createNode(() -> Animations.SWEEPING_EDGE).setCondition(new JumpCondition());
        ComboNode aaab = ComboNode.createNode(() -> Animations.BIPED_STEP_BACKWARD).setCondition(new JumpCondition());
        a.key1(aa);
        a.key2(ab);
        aa.key1(aaa);
        aa.key2(aab);
        aaa.key1(aaaa);
        aaa.key2(aaab);
        root.key1(a);
        COMBO_DEMO = registryWorker.build("combo_demo", ComboBasicAttack::new, ComboBasicAttack.createComboBasicAttack().setCombo(root).setShouldDrawGui(false));

        //You can also create the tree like this:
        //你也可以这样构建：
        ComboNode root2 = ComboNode.createRoot()
                .key1(ComboNode.createNode(() -> Animations.SWORD_AUTO1)
                        .key1(ComboNode.createNode(() -> Animations.SWORD_AUTO2)
                                .key1(() -> Animations.SWORD_AUTO3).setCondition(new JumpCondition())
                                .key2(() -> Animations.LONGSWORD_AUTO3))
                        .key2(() -> Animations.SWORD_AUTO2))
                .key2(ComboNode.createNode(() -> Animations.LONGSWORD_AUTO1));

        //TODO 数据包注册


    }

}
