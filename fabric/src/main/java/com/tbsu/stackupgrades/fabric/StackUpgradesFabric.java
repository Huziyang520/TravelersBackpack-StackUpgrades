/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.fabric;

import com.tiviacz.travelersbackpack.init.ModItemGroups;
import com.tbsu.stackupgrades.ModItems;
import com.tbsu.stackupgrades.StackUpgrades;
import com.tbsu.stackupgrades.item.StackUpgradeItem;
import com.tbsu.stackupgrades.loot.StackUpgradeLoot;
import com.tbsu.stackupgrades.upgrade.StackTier;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Fabric entrypoint: item registration, loot injection and the creative tab.
 *
 * <p>Registration has to happen here rather than in the shared sources: the
 * same classes are recompiled against NeoForge, which registers items through
 * its own deferred system.</p>
 */
public final class StackUpgradesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Only touched when the library is present, so its classes never load otherwise.
        if (FabricLoader.getInstance().isModLoaded(FabricConfig.FORGE_CONFIG_API_PORT)) {
            FabricConfig.register();
        }

        for (StackTier tier : StackTier.values()) {
            StackUpgradeItem item = ModItems.create(tier);
            Registry.register(BuiltInRegistries.ITEM, StackUpgrades.id(tier.itemName()), item);
            ModItems.remember(item);
        }

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) ->
                StackUpgradeLoot.poolsFor(key).forEach(tableBuilder::withPool));

        // Add our items to Traveler's Backpack own creative tab.
        CreativeModeTabEvents.modifyOutputEvent(ModItemGroups.TRAVELERS_BACKPACK)
                .register(output -> ModItems.ALL.forEach(output::accept));

        StackUpgrades.LOGGER.info("Stack upgrades registered ({} tiers, non-stackable items: {}, max slot limit: {})",
                ModItems.ALL.size(),
                StackUpgradesConfig.allowNonStackableItems ? "enabled" : "disabled",
                StackUpgradesConfig.maxSlotLimit);
    }
}
