/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.neoforge;

import com.tiviacz.travelersbackpack.init.ModCreativeTabs;
import com.tbsu.stackupgrades.ModItems;
import com.tbsu.stackupgrades.StackUpgrades;
import com.tbsu.stackupgrades.item.StackUpgradeItem;
import com.tbsu.stackupgrades.loot.StackUpgradeLoot;
import com.tbsu.stackupgrades.upgrade.StackTier;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.minecraft.world.level.storage.loot.LootPool;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * NeoForge entrypoint: items, config, loot and the creative tab.
 *
 * <p>Item instances are built up front so {@link ModItems} is filled before any
 * loot table or creative tab asks for them; the registry only defers the id
 * registration.</p>
 */
@Mod(StackUpgrades.MOD_ID)
public final class StackUpgradesNeoForge {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StackUpgrades.MOD_ID);

    public StackUpgradesNeoForge(IEventBus modBus, ModContainer container) {
        for (StackTier tier : StackTier.values()) {
            StackUpgradeItem item = ModItems.create(tier);
            ModItems.remember(item);
            ITEMS.register(tier.itemName(), () -> item);
        }
        ITEMS.register(modBus);

        NeoForgeConfig.register(container, modBus);

        // The screen factory is only resolved by the mod list, i.e. on the client,
        // so no dist check is needed here.
        Supplier<IConfigScreenFactory> screenFactory = () -> ConfigurationScreen::new;
        container.registerExtensionPoint(IConfigScreenFactory.class, screenFactory);

        modBus.addListener(StackUpgradesNeoForge::addCreativeTabEntries);
        NeoForge.EVENT_BUS.addListener(StackUpgradesNeoForge::addLootPools);

        StackUpgrades.LOGGER.info("Stack upgrades registered ({} tiers, non-stackable items: {}, max slot limit: {})",
                ModItems.ALL.size(),
                StackUpgradesConfig.allowNonStackableItems ? "enabled" : "disabled",
                StackUpgradesConfig.maxSlotLimit);
    }

    private static void addCreativeTabEntries(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(ModCreativeTabs.TRAVELERS_BACKPACK.getKey())) {
            return;
        }
        ModItems.ALL.forEach(event::accept);
    }

    private static void addLootPools(LootTableLoadEvent event) {
        for (LootPool.Builder pool : StackUpgradeLoot.poolsFor(event.getKey())) {
            event.getTable().addPool(pool.build());
        }
    }
}
