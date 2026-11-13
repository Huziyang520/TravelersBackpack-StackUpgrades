/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

/**
 * Values shared by every side.
 *
 * <p>The TOML file and its screen are provided by each loader: NeoForge's own
 * config API, and Forge Config API Port on Fabric (both optional there, the
 * defaults below are used when it is missing).</p>
 *
 * <p>Fields are written by the loader's config bootstrap after the config loads,
 * so every reader can keep taking them directly.</p>
 */
public final class StackUpgradesConfig {
    public static final int DEFAULT_MAX_SLOT_LIMIT = 1_073_741_824;
    public static final int HARD_MAX_SLOT_LIMIT = 1_073_741_824;

    public static boolean allowNonStackableItems = true;
    public static boolean allowDuplicateStackUpgrades = true;
    public static int maxSlotLimit = DEFAULT_MAX_SLOT_LIMIT;

    public static boolean lootEnabled = true;
    public static double lootChanceMultiplier = 1.0D;
    public static double lootTier4Chance = 0.01D;

    /**
     * Shift-click merges items whose max stack size is not 64.
     *
     * <p>Both switches below are read while the server performs the action
     * (shift-click transfer and the backpack sorter both run server side), so
     * they live in the common config: a client-only config would be ignored on
     * a dedicated server.</p>
     */
    public static boolean quickTransferStacking = true;
    /** The sort button merges up to the upgraded capacity. */
    public static boolean sorterStacking = true;

    private StackUpgradesConfig() {
    }
}
