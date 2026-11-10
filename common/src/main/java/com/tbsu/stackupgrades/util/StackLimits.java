/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.menu.slot.SlotItemHandler;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tbsu.stackupgrades.upgrade.StackUpgrade;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * All stack limit math lives here so that the mixins stay tiny.
 * <p>
 * The multiplier handling mirrors Sophisticated Backpacks: every installed stack
 * upgrade multiplies the slot capacity, and the capacity of a slot is
 * {@code floor(itemMaxStackSize * multiplier)}.
 */
public final class StackLimits {
    /** Vanilla slot capacity used as the base while no upgrade is installed. */
    public static final int BASE_LIMIT = 64;

    private StackLimits() {
    }

    /**
     * @return the combined multiplier of all stack upgrades installed in the given backpack (1.0 if none).
     */
    public static double multiplier(BackpackWrapper wrapper) {
        if (wrapper == null) {
            return 1.0D;
        }
        UpgradeManager manager = wrapper.getUpgradeManager();
        if (manager == null || manager.upgrades == null) {
            return 1.0D;
        }

        double multiplier = 1.0D;
        for (UpgradeBase<?> upgrade : manager.upgrades) {
            if (upgrade instanceof StackUpgrade stackUpgrade) {
                multiplier *= stackUpgrade.getMultiplier();
            }
        }
        return multiplier;
    }

    /**
     * Slot capacity for a stack of {@code itemMax} items inside a backpack with the given multiplier.
     */
    public static int capacity(int itemMax, double multiplier) {
        if (itemMax <= 1 && !StackUpgradesConfig.allowNonStackableItems) {
            return 1;
        }

        long value = (long) Math.floor(itemMax * multiplier);
        value = Math.min(value, StackUpgradesConfig.maxSlotLimit);
        return (int) Math.max(1L, value);
    }

    /**
     * True when the slot range [start, end) of the given menu holds at least one
     * backpack storage slot whose capacity is above vanilla.
     */
    public static boolean hasUpgradedSlot(AbstractContainerMenu menu, int start, int end) {
        int from = Math.max(0, start);
        int to = Math.min(menu.slots.size(), end);
        for (int i = from; i < to; i++) {
            Slot slot = menu.slots.get(i);
            if (!(slot instanceof SlotItemHandler handlerSlot)) {
                continue;
            }
            if (handlerSlot.getItemHandler() instanceof StackLimitProvider provider) {
                BackpackWrapper wrapper = provider.tbsu$getBackpackWrapper();
                if (wrapper != null && multiplier(wrapper) > 1.0D) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Slot capacity for the given stack inside a backpack with the given multiplier.
     */
    public static int capacity(ItemStack stack, double multiplier) {
        return capacity(stack.isEmpty() ? BASE_LIMIT : stack.getMaxStackSize(), multiplier);
    }
}
