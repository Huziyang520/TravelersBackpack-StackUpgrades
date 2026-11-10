/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import com.tbsu.stackupgrades.StackUpgrades;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Tiny properties based configuration, kept intentionally simple so that it can
 * never break world loading.
 *
 * <ul>
 *     <li>{@code allowNonStackableItems} - when true (default), items that
 *     normally do not stack at all (max stack size 1, e.g. tools) also receive
 *     the multiplier. Set to false to keep them unstackable.</li>
 *     <li>{@code allowDuplicateStackUpgrades} - when true (default) the same
 *     stack upgrade can be installed more than once, and every copy multiplies
 *     the slot capacity again.</li>
 *     <li>{@code maxSlotLimit} - hard upper bound for a single backpack slot.
 *     Six tier 4 upgrades give 64 * 16^6 = 1,073,741,824, which is the default
 *     upper bound (a netherite backpack has six upgrade slots).</li>
 * </ul>
 */
public final class StackUpgradesConfig {
    private static final String FILE_NAME = "tbstackupgrades.properties";

    /** Default allowed by six copies of the tier 4 upgrade (all upgrade slots). */
    public static final int DEFAULT_MAX_SLOT_LIMIT = 1_073_741_824;
    /** Absolute ceiling accepted from the config file. */
    public static final int HARD_MAX_SLOT_LIMIT = 1_073_741_824;

    public static boolean allowNonStackableItems = true;
    public static boolean allowDuplicateStackUpgrades = true;
    public static int maxSlotLimit = DEFAULT_MAX_SLOT_LIMIT;

    /** Server side: inject the upgrades into vanilla structure loot. */
    public static boolean lootEnabled = true;
    /** Scales every loot chance; 1.0 keeps the defaults. */
    public static double lootChanceMultiplier = 1.0D;
    /** Chance of the tier 4 upgrade in bastion treasure. */
    public static double lootTier4Chance = 0.01D;

    private StackUpgradesConfig() {
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        Properties properties = new Properties();

        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
            } catch (IOException e) {
                StackUpgrades.LOGGER.warn("Could not read {}, using defaults", path, e);
            }
        }

        // Configs written before 1.3.1 always carry the old "false" default for
        // allowNonStackableItems; migrate them once so the new default takes effect.
        // A configVersion marker keeps later user choices intact.
        int configVersion;
        try {
            configVersion = Integer.parseInt(properties.getProperty("configVersion", "1"));
        } catch (NumberFormatException e) {
            configVersion = 1;
        }
        boolean legacyConfig = configVersion < 2;

        allowNonStackableItems = legacyConfig
                || Boolean.parseBoolean(properties.getProperty("allowNonStackableItems", "true"));
        allowDuplicateStackUpgrades = Boolean.parseBoolean(properties.getProperty("allowDuplicateStackUpgrades", "true"));

        try {
            maxSlotLimit = Integer.parseInt(properties.getProperty("maxSlotLimit", Integer.toString(DEFAULT_MAX_SLOT_LIMIT)));
        } catch (NumberFormatException e) {
            maxSlotLimit = DEFAULT_MAX_SLOT_LIMIT;
        }
        // 8192 (1.0.0), 98304 (1.1.0) and 67108864 (1.2.0/1.3.0) were the old defaults
        // and would silently cut duplicated upgrades down again; treat them as
        // "not customised" so existing configs pick up the new default.
        if (maxSlotLimit == 8192 || maxSlotLimit == 98304 || maxSlotLimit == 67_108_864) {
            maxSlotLimit = DEFAULT_MAX_SLOT_LIMIT;
        }
        maxSlotLimit = Math.max(64, Math.min(maxSlotLimit, HARD_MAX_SLOT_LIMIT));

        lootEnabled = Boolean.parseBoolean(properties.getProperty("lootEnabled", "true"));
        try {
            lootChanceMultiplier = Double.parseDouble(properties.getProperty("lootChanceMultiplier", "1.0"));
        } catch (NumberFormatException e) {
            lootChanceMultiplier = 1.0D;
        }
        lootChanceMultiplier = Math.max(0.0D, Math.min(lootChanceMultiplier, 5.0D));
        try {
            lootTier4Chance = Double.parseDouble(properties.getProperty("lootTier4Chance", "0.01"));
        } catch (NumberFormatException e) {
            lootTier4Chance = 0.01D;
        }
        lootTier4Chance = Math.max(0.0D, Math.min(lootTier4Chance, 1.0D));

        properties.setProperty("allowNonStackableItems", Boolean.toString(allowNonStackableItems));
        properties.setProperty("allowDuplicateStackUpgrades", Boolean.toString(allowDuplicateStackUpgrades));
        properties.setProperty("maxSlotLimit", Integer.toString(maxSlotLimit));
        properties.setProperty("lootEnabled", Boolean.toString(lootEnabled));
        properties.setProperty("lootChanceMultiplier", Double.toString(lootChanceMultiplier));
        properties.setProperty("lootTier4Chance", Double.toString(lootTier4Chance));
        properties.setProperty("configVersion", "3");

        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                properties.store(out, "Traveler's Backpack: Stack Upgrades");
            }
        } catch (IOException e) {
            StackUpgrades.LOGGER.warn("Could not write config {}", path, e);
        }
    }
}
