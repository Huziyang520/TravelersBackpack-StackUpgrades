/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades;

import com.tbsu.stackupgrades.item.StackUpgradeItem;
import com.tbsu.stackupgrades.upgrade.StackTier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The five upgrade items. Instances are built and remembered here; putting them
 * into the item registry is loader specific and happens in the loader modules.
 */
public final class ModItems {
    public static final List<StackUpgradeItem> ALL = new ArrayList<>();
    private static final Map<StackTier, StackUpgradeItem> BY_TIER = new EnumMap<>(StackTier.class);

    private ModItems() {
    }

    /**
     * Builds the item of a tier. The id has to be set on the properties as well:
     * the game refuses a registered item whose stack id does not match its key.
     */
    public static StackUpgradeItem create(StackTier tier) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, StackUpgrades.id(tier.itemName()));
        // normal 64-per-stack item; the upgrade slot itself still accepts only one
        // (Traveler's Backpack reports a slot limit of 1 for its upgrade handler)
        return new StackUpgradeItem(new Item.Properties().stacksTo(64).rarity(tier.rarity()).setId(key), tier);
    }

    public static void remember(StackUpgradeItem item) {
        ALL.add(item);
        BY_TIER.put(item.getTier(), item);
    }

    public static StackUpgradeItem get(StackTier tier) {
        return BY_TIER.get(tier);
    }
}
