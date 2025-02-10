package com.p1nero.invincible;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.IntValue EFFECT_TICK = BUILDER.comment("重置招架成功/闪避成功判定的时间", "即招架成功/闪避成功结束后多长时间内符合对应Condition的使用条件。").defineInRange("effect_tick", 20, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue RESET_TICK = BUILDER.comment("重置连段时间", "即动作结束后多长时间后自动重置普攻连段。").defineInRange("reset_tick", 16, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue RESERVE_TICK = BUILDER.comment("预存输入时间，仅客户端有效", "即在前一个动画未结束时，若提前按下按键，则会在这段时间内反复尝试请求执行。").defineInRange("reserve_tick", 8, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue INPUT_DELAY_TICK = BUILDER.comment("输入延迟时间，仅客户端有效。指按下按键后到执行技能的时间间隔。", "越长则对双键判定越有利，但单键触发时长也会受影响。").defineInRange("input_delay_tick", 4, 0, Integer.MAX_VALUE);
    static final ForgeConfigSpec SPEC = BUILDER.build();

}
