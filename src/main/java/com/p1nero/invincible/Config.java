package com.p1nero.invincible;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.IntValue EFFECT_TICK = BUILDER.comment("重置招架成功/闪避成功判定的时间", "即招架成功/闪避成功结束后多长时间后自动重置普攻连段。").defineInRange("effect_tick", 20, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue RESET_TICK = BUILDER.comment("重置连段时间", "即动作结束后多长时间后自动重置普攻连段。").defineInRange("reset_tick", 16, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue RESERVE_TICK = BUILDER.comment("预存输入时间，仅客户端有效", "即在前一个动画未结束时，若提前按下按键，则会在这段时间内反复尝试请求执行。").defineInRange("reserve_tick", 8, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue PRESS_PROTECT_TICK = BUILDER.comment("长按保护时间。在新版本的无坚不摧当中，新加入了长按检测，因此发包改为抬手时发包。若长按时间超过长按保护时间，且本节点不存在长按检测条件时，将跳过节点的触发。").defineInRange("press_protect_tick", 20, 1, Integer.MAX_VALUE);
    static final ForgeConfigSpec SPEC = BUILDER.build();

}
