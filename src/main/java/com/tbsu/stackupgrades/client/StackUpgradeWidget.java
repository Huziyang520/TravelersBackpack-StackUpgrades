/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.client;

import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.widgets.UpgradeWidgetBase;
import com.tiviacz.travelersbackpack.client.screens.widgets.WidgetBase;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import com.tbsu.stackupgrades.upgrade.StackUpgrade;

/**
 * Sidebar widget for a stack upgrade. Stack upgrades have no tab of their own and
 * the widget of this version has no tooltip hook, so the extra lines are added by
 * the tooltip mixin of this branch.
 */
public class StackUpgradeWidget extends UpgradeWidgetBase<StackUpgrade> {
    public StackUpgradeWidget(BackpackScreen screen, StackUpgrade upgrade, Point pos) {
        super(screen, upgrade, pos, new Point(0, 0), "screen.tbstackupgrades.stack_upgrade");
    }

    // Return type is the base type so callers never have to name this class.
    public static WidgetBase create(BackpackScreen screen, StackUpgrade upgrade, int x, int y) {
        return new StackUpgradeWidget(screen, upgrade, new Point(screen.getGuiLeft() + x, screen.getGuiTop() + y));
    }
}
