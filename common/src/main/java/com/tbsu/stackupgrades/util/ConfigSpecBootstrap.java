/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import com.tbsu.stackupgrades.StackUpgrades;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.config.ModConfig;

/**
 * Builds the TOML config and keeps {@link StackUpgradesConfig} in sync with it.
 *
 * <p>This class is only loaded when Forge Config API Port is installed, so the
 * mod keeps working (with default values) on a setup without it.</p>
 */
final class ConfigSpecBootstrap {
    private static final ModConfigSpec.Builder COMMON = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT = new ModConfigSpec.Builder();

    static final ModConfigSpec COMMON_SPEC;
    static final ModConfigSpec CLIENT_SPEC;

    private static final ModConfigSpec.BooleanValue ALLOW_NON_STACKABLE;
    private static final ModConfigSpec.BooleanValue ALLOW_DUPLICATES;
    private static final ModConfigSpec.IntValue MAX_SLOT_LIMIT;
    private static final ModConfigSpec.BooleanValue LOOT_ENABLED;
    private static final ModConfigSpec.DoubleValue LOOT_CHANCE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue LOOT_TIER4_CHANCE;
    private static final ModConfigSpec.BooleanValue QUICK_TRANSFER_STACKING;
    private static final ModConfigSpec.BooleanValue SORTER_STACKING;

    static {
        COMMON.push("limits");
        ALLOW_NON_STACKABLE = COMMON
                .comment("Items whose max stack size is 1 (tools, armor) also use the multiplier.")
                .define("allowNonStackableItems", true);
        ALLOW_DUPLICATES = COMMON
                .comment("Allow installing the same stack upgrade tier more than once.")
                .define("allowDuplicateStackUpgrades", true);
        MAX_SLOT_LIMIT = COMMON
                .comment("Hard cap for a single backpack slot.")
                .defineInRange("maxSlotLimit", StackUpgradesConfig.DEFAULT_MAX_SLOT_LIMIT, 64,
                        StackUpgradesConfig.HARD_MAX_SLOT_LIMIT);
        COMMON.pop();

        COMMON.push("loot");
        LOOT_ENABLED = COMMON
                .comment("Put the stack upgrades into structure loot.")
                .define("lootEnabled", true);
        LOOT_CHANCE_MULTIPLIER = COMMON
                .comment("Scales every loot chance.")
                .defineInRange("lootChanceMultiplier", 1.0D, 0.0D, 5.0D);
        LOOT_TIER4_CHANCE = COMMON
                .comment("Chance of the tier 4 upgrade in bastion treasure.")
                .defineInRange("lootTier4Chance", 0.01D, 0.0D, 1.0D);
        COMMON.pop();

        CLIENT.push("controls");
        QUICK_TRANSFER_STACKING = CLIENT
                .comment("Shift-clicking into the backpack merges items whose max stack size is not 64.")
                .define("quickTransferStacking", true);
        SORTER_STACKING = CLIENT
                .comment("The sort button merges stacks up to the upgraded capacity.")
                .define("sorterStacking", true);
        CLIENT.pop();

        COMMON_SPEC = COMMON.build();
        CLIENT_SPEC = CLIENT.build();
    }

    private ConfigSpecBootstrap() {
    }

    static void register() {
        ConfigRegistry.INSTANCE.register(StackUpgrades.MOD_ID, ModConfig.Type.COMMON, COMMON_SPEC);
        ConfigRegistry.INSTANCE.register(StackUpgrades.MOD_ID, ModConfig.Type.CLIENT, CLIENT_SPEC);
        ModConfigEvents.loading(StackUpgrades.MOD_ID).register(ConfigSpecBootstrap::apply);
        ModConfigEvents.reloading(StackUpgrades.MOD_ID).register(ConfigSpecBootstrap::apply);
    }

    private static void apply(ModConfig config) {
        switch (config.getType()) {
            case COMMON, SERVER -> {
                StackUpgradesConfig.allowNonStackableItems = ALLOW_NON_STACKABLE.get();
                StackUpgradesConfig.allowDuplicateStackUpgrades = ALLOW_DUPLICATES.get();
                StackUpgradesConfig.maxSlotLimit = MAX_SLOT_LIMIT.get();
                StackUpgradesConfig.lootEnabled = LOOT_ENABLED.get();
                StackUpgradesConfig.lootChanceMultiplier = LOOT_CHANCE_MULTIPLIER.get();
                StackUpgradesConfig.lootTier4Chance = LOOT_TIER4_CHANCE.get();
            }
            case CLIENT -> {
                StackUpgradesConfig.quickTransferStacking = QUICK_TRANSFER_STACKING.get();
                StackUpgradesConfig.sorterStacking = SORTER_STACKING.get();
            }
            default -> {
            }
        }
    }
}
