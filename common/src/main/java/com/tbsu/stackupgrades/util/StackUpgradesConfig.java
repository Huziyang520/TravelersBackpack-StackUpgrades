/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import net.fabricmc.loader.api.FabricLoader;


/**
 * Values shared by every side. The TOML config and its screen are provided by
 * Forge Config API Port when it is installed; without it the defaults below are
 * used as-is, so the mod never hard-depends on that library.
 *
 * <p>Fields are written by {@code ConfigSpecBootstrap} after the config loads, so
 * every reader can keep taking them directly.</p>
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

    /** Client side switch: shift-click merges items whose max stack size is not 64. */
    public static boolean quickTransferStacking = true;
    /** Client side switch: the sort button merges up to the upgraded capacity. */
    public static boolean sorterStacking = true;

    private StackUpgradesConfig() {
    }

    /**
     * Registers the TOML config when Forge Config API Port is present, otherwise
     * imports nothing and keeps the defaults.
     */
    public static void load() {
        if (FabricLoader.getInstance().isModLoaded("forgeconfigapiport")) {
            ConfigSpecBootstrap.register();
        }
    }
}
