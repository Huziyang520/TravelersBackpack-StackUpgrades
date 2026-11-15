/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.forge;

import com.tbsu.stackupgrades.StackUpgrades;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

/**
 * Config screen registration on Forge.
 *
 * <p>Forge 1.20.1 only draws the mod list "Config" button for mods that register
 * a {@code ConfigScreenHandler.ConfigScreenFactory}, and Traveler's Backpack
 * ships no screen for this version. Neo Configuration Screen Port provides
 * NeoForge's configuration screen for Forge.</p>
 *
 * <p>The port stays optional: it is called through reflection, so nothing links
 * against it, and without it the mod keeps working with the TOML file only.</p>
 */
final class ForgeConfigScreen {
    private static final String PORT_MOD_ID = "neoconfigurationscreenport";
    private static final String REGISTRY =
            "com.huziyang.neoconfigurationscreenport.forge.ConfigScreenRegistration";

    private ForgeConfigScreen() {
    }

    static void register() {
        if (!ModList.get().isLoaded(PORT_MOD_ID)) {
            return;
        }
        try {
            Class<?> registry = Class.forName(REGISTRY);
            Method register = registry.getMethod("register", String.class);
            register.invoke(null, StackUpgrades.MOD_ID);
        } catch (ReflectiveOperationException | LinkageError e) {
            // An incompatible port version must not take the whole mod down.
            StackUpgrades.LOGGER.warn("Config screen not registered: {}", e.toString());
        }
    }
}
