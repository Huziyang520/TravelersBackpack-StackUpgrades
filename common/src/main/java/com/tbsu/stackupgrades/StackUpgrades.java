/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades;

import com.tbsu.stackupgrades.loot.StackUpgradeLoot;
import com.tbsu.stackupgrades.util.ClientOptimizations;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traveler's Backpack: Stack Upgrades.
 *
 * Adds tiered "Stack Upgrade" items that can be placed into the backpack's
 * upgrade sidebar. Each installed upgrade multiplies the amount of items that
 * can be held in a single backpack slot, mirroring the multipliers used by
 * Sophisticated Backpacks (1.5x / 2x / 4x / 8x / 16x).
 */
public class StackUpgrades implements ModInitializer {
    public static final String MOD_ID = "tbstackupgrades";
    public static final Logger LOGGER = LoggerFactory.getLogger("Traveler'sBackpack-StackUpgrades");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        StackUpgradesConfig.load();
        ClientOptimizations.load();
        ModItems.register();
        StackUpgradeLoot.register();

        // Add our items to Traveler's Backpack own creative tab.
        CreativeModeTabEvents.modifyOutputEvent(com.tiviacz.travelersbackpack.init.ModItemGroups.TRAVELERS_BACKPACK)
                .register(output -> ModItems.ALL.forEach(output::accept));

        LOGGER.info("Stack upgrades registered ({} tiers, non-stackable items: {}, max slot limit: {})",
                ModItems.ALL.size(),
                StackUpgradesConfig.allowNonStackableItems ? "enabled" : "disabled",
                StackUpgradesConfig.maxSlotLimit);
    }
}
