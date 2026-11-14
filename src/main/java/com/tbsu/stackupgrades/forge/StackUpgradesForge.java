/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.forge;

import com.tiviacz.travelersbackpack.init.ModCreativeTabs;
import com.tbsu.stackupgrades.ModItems;
import com.tbsu.stackupgrades.StackUpgrades;
import com.tbsu.stackupgrades.item.StackUpgradeItem;
import com.tbsu.stackupgrades.loot.StackUpgradeLoot;
import com.tbsu.stackupgrades.upgrade.StackTier;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Forge entrypoint: items, config, loot and the creative tab.
 *
 * <p>Item instances are built up front so {@link ModItems} is filled before any
 * loot table or creative tab asks for them; the registry only defers the id
 * registration.</p>
 */
@Mod(StackUpgrades.MOD_ID)
public final class StackUpgradesForge {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, StackUpgrades.MOD_ID);

    public StackUpgradesForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        for (StackTier tier : StackTier.values()) {
            // The item is constructed at the register event: the item registry is
            // already frozen while mods are being constructed.
            ITEMS.register(tier.itemName(), () -> {
                StackUpgradeItem item = ModItems.create(tier);
                ModItems.remember(item);
                return item;
            });
        }
        ITEMS.register(modBus);

        ForgeConfig.register(ModLoadingContext.get(), modBus);

        modBus.addListener(StackUpgradesForge::addCreativeTabEntries);
        modBus.addListener(StackUpgradesForge::logConfiguration);
        MinecraftForge.EVENT_BUS.addListener(StackUpgradesForge::addLootPools);
    }

    private static void logConfiguration(FMLCommonSetupEvent event) {
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
        for (LootPool.Builder pool : StackUpgradeLoot.poolsFor(event.getName())) {
            event.getTable().addPool(pool.build());
        }
    }
}

