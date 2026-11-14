/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.forge;

import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * TOML config on Forge, using the loader's own config API.
 *
 * <p>Every key is read back into {@link StackUpgradesConfig} on load and reload,
 * so the rest of the code keeps reading plain static fields.</p>
 */
public final class ForgeConfig {
    private static final ForgeConfigSpec.Builder COMMON = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ALLOW_NON_STACKABLE;
    private static final ForgeConfigSpec.BooleanValue ALLOW_DUPLICATES;
    private static final ForgeConfigSpec.IntValue MAX_SLOT_LIMIT;
    private static final ForgeConfigSpec.BooleanValue LOOT_ENABLED;
    private static final ForgeConfigSpec.DoubleValue LOOT_CHANCE_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue LOOT_TIER4_CHANCE;
    private static final ForgeConfigSpec.BooleanValue QUICK_TRANSFER_STACKING;
    private static final ForgeConfigSpec.BooleanValue SORTER_STACKING;
    private static final ForgeConfigSpec.BooleanValue RIGHT_CLICK_PICKUP_LIMIT;
    private static final ForgeConfigSpec.BooleanValue SHIFT_TRANSFER_LIMIT;

    public static final ForgeConfigSpec COMMON_SPEC;

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

        COMMON.push("controls");
        QUICK_TRANSFER_STACKING = COMMON
                .comment("Shift-clicking into the backpack merges items whose max stack size is not 64.")
                .define("quickTransferStacking", true);
        SORTER_STACKING = COMMON
                .comment("The sort button merges stacks up to the upgraded capacity.")
                .define("sorterStacking", true);
        RIGHT_CLICK_PICKUP_LIMIT = COMMON
                .comment("Right-clicking an oversized stack takes 32 items instead of half of it.")
                .define("rightClickPickupLimit", true);
        SHIFT_TRANSFER_LIMIT = COMMON
                .comment("Shift-clicking a slot that holds more than a normal stack moves one stack and leaves the rest.")
                .define("shiftTransferLimit", true);
        COMMON.pop();

        COMMON_SPEC = COMMON.build();
    }

    private ForgeConfig() {
    }

    public static void register(net.minecraftforge.fml.ModLoadingContext context,
                                net.minecraftforge.eventbus.api.IEventBus modBus) {
        context.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        modBus.register(ForgeConfig.class);
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        apply(event.getConfig());
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        apply(event.getConfig());
    }

    private static void apply(ModConfig config) {
        if (config == null || config.getSpec() != COMMON_SPEC) {
            return;
        }
        StackUpgradesConfig.allowNonStackableItems = ALLOW_NON_STACKABLE.get();
        StackUpgradesConfig.allowDuplicateStackUpgrades = ALLOW_DUPLICATES.get();
        StackUpgradesConfig.maxSlotLimit = MAX_SLOT_LIMIT.get();
        StackUpgradesConfig.lootEnabled = LOOT_ENABLED.get();
        StackUpgradesConfig.lootChanceMultiplier = LOOT_CHANCE_MULTIPLIER.get();
        StackUpgradesConfig.lootTier4Chance = LOOT_TIER4_CHANCE.get();
        StackUpgradesConfig.quickTransferStacking = QUICK_TRANSFER_STACKING.get();
        StackUpgradesConfig.sorterStacking = SORTER_STACKING.get();
        StackUpgradesConfig.rightClickPickupLimit = RIGHT_CLICK_PICKUP_LIMIT.get();
        StackUpgradesConfig.shiftTransferLimit = SHIFT_TRANSFER_LIMIT.get();
    }
}
