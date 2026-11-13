/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

/**
 * Click-related state for the menu tweaks.
 *
 * <p>An oversized slot cannot be capped by changing the item's max stack size,
 * so the amount a click moves is clamped where the click is executed. The mouse
 * button is not a parameter of that call, hence the thread-local.</p>
 */
public final class ClickLimits {
    /** Items taken by a right click while the option is on. */
    public static final int RIGHT_CLICK_PICKUP = 32;

    private static final ThreadLocal<Integer> BUTTON = new ThreadLocal<>();

    private ClickLimits() {
    }

    public static void setButton(int button) {
        BUTTON.set(button);
    }

    public static void clearButton() {
        BUTTON.remove();
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
