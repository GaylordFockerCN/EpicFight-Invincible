package com.p1nero.invincible.compat.controlify;

import com.p1nero.invincible.InvincibleMod;
import com.p1nero.invincible.client.InvincibleKeyMappings;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.RadialIcons;
import dev.isxander.controlify.utils.render.Blit;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public class ControlifyCompat implements ControlifyEntrypoint {
    private static InputBindingSupplier primaryAction;
    private static InputBindingSupplier secondaryAction;
    private static InputBindingSupplier specialAbility1;
    private static InputBindingSupplier specialAbility2;

    private static boolean isModInstalled;

    public static boolean isModInstalled() {
        return isModInstalled;
    }

    // Hack: Since some parts of Invincible Lib stores KeyMapping,
    // the KeyMapping is mapped to a Controlify input binding that can be used.
    // This HACK can be eliminated in MC versions newer than 1.21.10
    public static @Nullable InputBindingSupplier getInputBindingFromKeyMapping(@NotNull KeyMapping keyMapping) {
        if (keyMapping == InvincibleKeyMappings.KEY1) {
            return primaryAction;
        }
        if (keyMapping == InvincibleKeyMappings.KEY2) {
            return secondaryAction;
        }
        if (keyMapping == InvincibleKeyMappings.KEY3) {
            return specialAbility1;
        }
        if (keyMapping == InvincibleKeyMappings.KEY4) {
            return specialAbility2;
        }
        return null;
    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {

    }

    @Override
    public void onControlifyInit(InitContext context) {

    }

    @Override
    public void onControlifyPreInit(PreInitContext context) {
        isModInstalled = true;
        final ControlifyBindApi registrar = ControlifyBindApi.get();
        registerCustomRadialIcons();
        registrar.registerBindContext(IN_GAME_EPIC_FIGHT_CONTEXT);
        registerInputBindings(registrar);
    }

    private static void registerInputBindings(ControlifyBindApi registrar) {
        primaryAction = registrar.registerBinding(
                builder -> builder.id(InvincibleMod.rl("primary_action"))
                        .category(ComponentConstants.COMMON_CATEGORY)
                        .allowedContexts(IN_GAME_EPIC_FIGHT_CONTEXT)
                        .name(ComponentConstants.PRIMARY_ACTION)
                        .description(ComponentConstants.PRIMARY_ACTION_DESCRIPTION)
                        .addKeyCorrelation(InvincibleKeyMappings.KEY1)
                        .keyEmulation(InvincibleKeyMappings.KEY1)
        );
        secondaryAction = registrar.registerBinding(
                builder -> builder.id(InvincibleMod.rl("secondary_action"))
                        .category(ComponentConstants.COMMON_CATEGORY)
                        .allowedContexts(IN_GAME_EPIC_FIGHT_CONTEXT)
                        .name(ComponentConstants.SECONDARY_ACTION)
                        .description(ComponentConstants.SECONDARY_ACTION_DESCRIPTION)
                        .addKeyCorrelation(InvincibleKeyMappings.KEY2)
                        .keyEmulation(InvincibleKeyMappings.KEY2)
        );
        specialAbility1 = registrar.registerBinding(
                builder -> builder.id(InvincibleMod.rl("special_ability_1"))
                        .category(ComponentConstants.COMMON_CATEGORY)
                        .allowedContexts(IN_GAME_EPIC_FIGHT_CONTEXT)
                        .name(ComponentConstants.SPECIAL_ABILITY_1)
                        .description(ComponentConstants.SPECIAL_ABILITY_1_DESCRIPTION)
                        .addKeyCorrelation(InvincibleKeyMappings.KEY3)
                        .keyEmulation(InvincibleKeyMappings.KEY3)
                        .radialCandidate(InvincibleRadialIcons.COMBO_ATTACKS.getId())
        );
        specialAbility2 = registrar.registerBinding(
                builder -> builder.id(InvincibleMod.rl("special_ability_2"))
                        .category(ComponentConstants.COMMON_CATEGORY)
                        .allowedContexts(IN_GAME_EPIC_FIGHT_CONTEXT)
                        .name(ComponentConstants.SPECIAL_ABILITY_2)
                        .description(ComponentConstants.SPECIAL_ABILITY_2_DESCRIPTION)
                        .addKeyCorrelation(InvincibleKeyMappings.KEY4)
                        .keyEmulation(InvincibleKeyMappings.KEY4)
                        .radialCandidate(InvincibleRadialIcons.COMBO_ATTACKS.getId())
        );
    }

    private enum InvincibleRadialIcons {
        COMBO_ATTACKS(InvincibleMod.rl("textures/gui/skills/weapon_innate/combo_demo.png"));

        private final @NotNull ResourceLocation id;

        InvincibleRadialIcons(@NotNull ResourceLocation id) {
            this.id = id;
        }

        public @NotNull ResourceLocation getId() {
            return id;
        }
    }

    private static void registerCustomRadialIcons() {
        for (InvincibleRadialIcons icon : InvincibleRadialIcons.values()) {
            final ResourceLocation location = icon.getId();
            RadialIcons.registerIcon(location, (graphics, x, y, tickDelta) -> {
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale(0.5f, 0.5f, 1f);
                Blit.blitTex(graphics, location, 0, 0, 0, 0, 32, 32, 32, 32);
                graphics.pose().popPose();
            });
        }
    }

    private static final BindContext IN_GAME_EPIC_FIGHT_CONTEXT = new BindContext(
            InvincibleMod.rl("epicfight_combat"),
            mc -> {
                final boolean isInGame = mc.screen == null && mc.level != null && mc.player != null;
                if (isInGame) {
                    final LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getEntityPatch(mc.player, LocalPlayerPatch.class);
                    if (localPlayerPatch == null) {
                        return false;
                    }
                    return localPlayerPatch.isEpicFightMode();
                }
                return false;
            }
    );

    private static class ComponentConstants {
        private static final Component COMMON_CATEGORY = Component.translatable("key.invincible.category");

        // Names
        private static final Component PRIMARY_ACTION = Component.translatable("key.invincible.key1");
        private static final Component SECONDARY_ACTION = Component.translatable("key.invincible.key2");
        private static final Component SPECIAL_ABILITY_1 = Component.translatable("key.invincible.key3");
        private static final Component SPECIAL_ABILITY_2 = Component.translatable("key.invincible.key4");

        // Descriptions
        private static final Component PRIMARY_ACTION_DESCRIPTION = Component.translatable("key.invincible.key1.description");
        private static final Component SECONDARY_ACTION_DESCRIPTION = Component.translatable("key.invincible.key2.description");
        private static final Component SPECIAL_ABILITY_1_DESCRIPTION = Component.translatable("key.invincible.key3.description");
        private static final Component SPECIAL_ABILITY_2_DESCRIPTION = Component.translatable("key.invincible.key4.description");
    }
}
