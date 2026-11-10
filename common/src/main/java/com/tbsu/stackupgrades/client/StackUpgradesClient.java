/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.client;

import com.tbsu.stackupgrades.StackUpgrades;
import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/**
 * Adds the config button to Mod Menu. Both Mod Menu and Forge Config API Port
 * are optional, so the screen is only wired up when both are installed.
 */
public final class StackUpgradesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (!FabricLoader.getInstance().isModLoaded("modmenu")
                || !FabricLoader.getInstance().isModLoaded("forgeconfigapiport")) {
            return;
        }
        ConfigScreenFactoryRegistry.INSTANCE.register(StackUpgrades.MOD_ID,
                (modId, parent) -> new ConfigurationScreen(modId, parent));
    }
}
