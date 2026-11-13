/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import net.minecraft.world.item.ItemStack;

/**
 * State of the click that is currently being handled.
 *
 * <p>An oversized slot cannot be capped through the item's max stack size, so the
 * amount a click moves is limited while it is being moved. The values are tracked
 * per thread because the click handler and the transfer share no parameters.</p>
 *
 * <p>The transfer is capped by shrinking the capacity the destination slots offer,
 * never by rewriting the source stack: the source keeps its remainder in place,
 * which keeps the slot contents and the stored backpack data in sync.</p>
 */
public final class ClickLimits {
    /** Items taken by a right click while the option is on. */
    public static final int RIGHT_CLICK_PICKUP = 32;

    private static final ThreadLocal<Integer> BUTTON = new ThreadLocal<>();
    private static final ThreadLocal<Integer> BUDGET = new ThreadLocal<>();
    private static final ThreadLocal<Integer> SPENT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> MOVE_START = new ThreadLocal<>();
    private static final ThreadLocal<ItemStack> MOVE_STACK = new ThreadLocal<>();

    private ClickLimits() {
    }

    /**
     * Starts the click state.
     *
     * @param budget items this click may move in total, 0 for no limit
     */
    public static void beginClick(int button, int budget) {
        BUTTON.set(button);
        if (budget > 0) {
            BUDGET.set(budget);
            SPENT.set(0);
        } else {
            BUDGET.remove();
            SPENT.remove();
        }
    }

    public static void endClick() {
        BUTTON.remove();
        BUDGET.remove();
        SPENT.remove();
        MOVE_START.remove();
        MOVE_STACK.remove();
    }

    /** Remembers the stack a transfer is about to move. */
    public static void beginMove(ItemStack stack) {
        MOVE_STACK.set(stack);
        MOVE_START.set(stack.getCount());
    }

    /** Adds what this transfer actually moved to the budget spent so far. */
    public static void endMove() {
        Integer start = MOVE_START.get();
        ItemStack stack = MOVE_STACK.get();
        Integer spent = SPENT.get();
        if (start != null && stack != null && spent != null) {
            SPENT.set(spent + Math.max(0, start - stack.getCount()));
        }
        MOVE_START.remove();
        MOVE_STACK.remove();
    }

    /**
     * Capacity a destination slot may offer during the current transfer, limited by
     * what is left of the click budget. Callers pass the real slot capacity.
     */
    public static int allowance(int actual, ItemStack argument) {
        Integer budget = BUDGET.get();
        Integer spent = SPENT.get();
        Integer start = MOVE_START.get();
        ItemStack moving = MOVE_STACK.get();
        if (budget == null || spent == null || start == null || moving == null) {
            return actual;
        }

        int used = Math.max(0, start - moving.getCount());
        int left = Math.max(0, budget - spent - used);
        // Merging into an existing stack is compared against the total the slot may
        // hold, so the stack already there is part of the allowance.
        int allowed = argument == moving ? left : argument.getCount() + left;
        return Math.min(actual, allowed);
    }

    /**
     * Amount to take for the current click.
     *
     * <p>A right click asks for half of the stack, which on an oversized stack is
     * far more than the player wants. Taking the whole stack is a left click and
     * stays untouched, so only requests below the available amount are clamped.</p>
     */
    public static int clampPickup(int amount, int available) {
        Integer button = BUTTON.get();
        if (!StackUpgradesConfig.rightClickPickupLimit || button == null || button != 1) {
            return amount;
        }
        if (amount >= available) {
            return amount;
        }
        return Math.min(amount, RIGHT_CLICK_PICKUP);
    }
}
