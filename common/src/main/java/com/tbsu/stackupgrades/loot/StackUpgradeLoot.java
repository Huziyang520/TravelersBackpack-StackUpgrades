/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.loot;

import com.tbsu.stackupgrades.ModItems;
import com.tbsu.stackupgrades.upgrade.StackTier;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the upgrades to vanilla structure loot as their own pools, so other mods'
 * pools and datapack overrides of the same table keep working. Chances follow
 * the addon's tier order: early structures only drop the low tiers.
 *
 * <p>The pools are built here, but attaching them to a table is loader specific
 * (Fabric modifies the table builder, NeoForge adds them to the loaded table),
 * so this class only hands the builders out.</p>
 */
public final class StackUpgradeLoot {
    private record Drop(ResourceKey<LootTable> table, StackTier tier, float chance) {
    }

    private static final List<Drop> DROPS = List.of(
            new Drop(BuiltInLootTables.ABANDONED_MINESHAFT, StackTier.STARTER, 0.04F),
            new Drop(BuiltInLootTables.ABANDONED_MINESHAFT, StackTier.TIER_1, 0.03F),
            new Drop(BuiltInLootTables.SIMPLE_DUNGEON, StackTier.STARTER, 0.04F),
            new Drop(BuiltInLootTables.SIMPLE_DUNGEON, StackTier.TIER_1, 0.03F),
            new Drop(BuiltInLootTables.DESERT_PYRAMID, StackTier.STARTER, 0.03F),
            new Drop(BuiltInLootTables.DESERT_PYRAMID, StackTier.TIER_1, 0.03F),
            new Drop(BuiltInLootTables.DESERT_PYRAMID, StackTier.TIER_2, 0.02F),
            new Drop(BuiltInLootTables.SHIPWRECK_TREASURE, StackTier.STARTER, 0.03F),
            new Drop(BuiltInLootTables.SHIPWRECK_TREASURE, StackTier.TIER_1, 0.03F),
            new Drop(BuiltInLootTables.SHIPWRECK_TREASURE, StackTier.TIER_2, 0.02F),
            new Drop(BuiltInLootTables.WOODLAND_MANSION, StackTier.TIER_1, 0.03F),
            new Drop(BuiltInLootTables.WOODLAND_MANSION, StackTier.TIER_2, 0.02F),
            new Drop(BuiltInLootTables.NETHER_BRIDGE, StackTier.TIER_2, 0.03F),
            new Drop(BuiltInLootTables.ANCIENT_CITY, StackTier.TIER_3, 0.015F),
            new Drop(BuiltInLootTables.BASTION_TREASURE, StackTier.TIER_2, 0.02F),
            new Drop(BuiltInLootTables.BASTION_TREASURE, StackTier.TIER_3, 0.015F),
            new Drop(BuiltInLootTables.BASTION_TREASURE, StackTier.TIER_4, 0.01F),
            new Drop(BuiltInLootTables.END_CITY_TREASURE, StackTier.TIER_2, 0.02F),
            new Drop(BuiltInLootTables.END_CITY_TREASURE, StackTier.TIER_3, 0.02F),
            new Drop(BuiltInLootTables.ABANDONED_CAMP_SECRET_CHEST, StackTier.STARTER, 0.03F),
            new Drop(BuiltInLootTables.ABANDONED_CAMP_SECRET_CHEST, StackTier.TIER_1, 0.02F)
    );

    private StackUpgradeLoot() {
    }

    /**
     * Loot pools to attach to the given table. Empty when the table is not in the
     * drop table, or when the related chances are turned off in the config.
     */
    public static List<LootPool.Builder> poolsFor(ResourceKey<LootTable> table) {
        if (!StackUpgradesConfig.lootEnabled) {
            return List.of();
        }

        List<LootPool.Builder> pools = new ArrayList<>(2);
        for (Drop drop : DROPS) {
            if (!drop.table().equals(table)) {
                continue;
            }
            float chance = drop.tier() == StackTier.TIER_4
                    ? (float) StackUpgradesConfig.lootTier4Chance
                    : drop.chance();
            chance *= (float) StackUpgradesConfig.lootChanceMultiplier;
            if (chance <= 0.0F) {
                continue;
            }
            pools.add(LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.get(drop.tier())))
                    .when(LootItemRandomChanceCondition.randomChance(chance)));
        }
        return pools;
    }
}
