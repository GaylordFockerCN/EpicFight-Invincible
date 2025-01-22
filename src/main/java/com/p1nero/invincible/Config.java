package com.p1nero.invincible;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = InvincibleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue EFFECT_TICK = BUILDER.comment("事件有效期，如闪避成功后处于闪避成功状态的时间").defineInRange("effect_tick", 16, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue RESET_TICK = BUILDER.comment("重置连段时间").defineInRange("reset_tick", 16, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue RESERVE_TICK = BUILDER.comment("预存输入时间，仅客户端有效").defineInRange("reserve_tick", 8, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue INPUT_DELAY_TICK = BUILDER.comment("输入延迟时间，仅客户端有效").defineInRange("input_delay_tick", 4, 0, Integer.MAX_VALUE);
    static final ForgeConfigSpec SPEC = BUILDER.build();

}
