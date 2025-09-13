package com.p1nero.invincible.command.arguments;

import com.p1nero.invincible.InvincibleMod;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class InvincibleCommandArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, InvincibleMod.MOD_ID);

    public static final RegistryObject<ArgumentTypeInfo<AllSkillArgument, ?>> SKILL = COMMAND_ARGUMENT_TYPES.register("skill", () -> SingletonArgumentInfo.contextFree(AllSkillArgument::skill));
    public static void registerArgumentTypes() {
        ArgumentTypeInfos.registerByClass(AllSkillArgument.class, SKILL.get());
    }
}
