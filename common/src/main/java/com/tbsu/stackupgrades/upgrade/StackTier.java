/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.upgrade;

import com.tbsu.stackupgrades.util.StackLimits;

/**
 * The five stack upgrade tiers. Multipliers are identical to the ones used by
 * Sophisticated Backpacks so that both mods feel the same.
 */
public enum StackTier {
    STARTER("stack_upgrade_starter", 1.5D),
    TIER_1("stack_upgrade_tier_1", 2.0D),
    TIER_2("stack_upgrade_tier_2", 4.0D),
    TIER_3("stack_upgrade_tier_3", 8.0D),
    TIER_4("stack_upgrade_tier_4", 16.0D);

    private final String itemName;
    private final double multiplier;

    StackTier(String itemName, double multiplier) {
        this.itemName = itemName;
        this.multiplier = multiplier;
    }

    public String itemName() {
        return this.itemName;
    }

    public double multiplier() {
        return this.multiplier;
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
