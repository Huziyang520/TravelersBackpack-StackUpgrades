/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.upgrade;

import com.tbsu.stackupgrades.util.StackLimits;
import net.minecraft.world.item.Rarity;

/**
 * The five stack upgrade tiers. Multipliers are identical to the ones used by
 * Sophisticated Backpacks so that both mods feel the same.
 */
public enum StackTier {
    STARTER("stack_upgrade_starter", 1.5D, Rarity.COMMON),
    TIER_1("stack_upgrade_tier_1", 2.0D, Rarity.COMMON),
    TIER_2("stack_upgrade_tier_2", 4.0D, Rarity.UNCOMMON),
    TIER_3("stack_upgrade_tier_3", 8.0D, Rarity.RARE),
    TIER_4("stack_upgrade_tier_4", 16.0D, Rarity.EPIC);

    private final String itemName;
    private final double multiplier;
    private final Rarity rarity;

    StackTier(String itemName, double multiplier, Rarity rarity) {
        this.itemName = itemName;
        this.multiplier = multiplier;
        this.rarity = rarity;
    }

    public String itemName() {
        return this.itemName;
    }

    public double multiplier() {
        return this.multiplier;
    }

    /** Name colour of the item; follows the material the recipe is built from. */
    public Rarity rarity() {
        return this.rarity;
    }

    /**
     * Capacity a normal (max 64) item gets with a single upgrade of this tier.
     */
    public int capacityFor64() {
        return StackLimits.capacity(64, this.multiplier);
    }

    public String translationKey() {
        return "item.tbstackupgrades." + this.itemName;
    }
}
