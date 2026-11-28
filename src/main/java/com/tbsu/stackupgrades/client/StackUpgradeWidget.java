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
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import com.tbsu.stackupgrades.upgrade.StackTier;
import com.tbsu.stackupgrades.upgrade.StackUpgrade;
import com.tbsu.stackupgrades.util.StackLimits;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Sidebar widget for a stack upgrade. Stack upgrades have no tab of their own,
 * so this widget only provides a tooltip for the icon inside the upgrade slot
 * (clicking it removes the upgrade, which is handled by the base class).
 */
public class StackUpgradeWidget extends UpgradeWidgetBase<StackUpgrade> {
    public StackUpgradeWidget(BackpackScreen screen, StackUpgrade upgrade, Point pos) {
        super(screen, upgrade, pos, new Point(0, 0), "screen.tbstackupgrades.stack_upgrade");
    }

    // Return type is the base type so callers never have to name this class.
    public static WidgetBase<?> create(BackpackScreen screen, StackUpgrade upgrade, int x, int y) {
        return new StackUpgradeWidget(screen, upgrade, new Point(screen.getGuiLeft() + x, screen.getGuiTop() + y));
    }

    @Override
    public void getAdditionalTooltips(Consumer<Component> consumer) {
        StackTier tier = this.upgrade.getTier();
        consumer.accept(Component.translatable("screen.tbstackupgrades.multiplier", format(tier.multiplier())));
        consumer.accept(Component.translatable("screen.tbstackupgrades.slot_limit", formatNumber(tier.capacityFor64())));

        // with duplicate upgrades the tier multiplier alone says nothing about the
        // real capacity, so show the combined value as well
        BackpackWrapper wrapper = this.upgrade.getUpgradeManager() == null
                ? null
                : this.upgrade.getUpgradeManager().getWrapper();
        if (wrapper != null) {
            int capacity = StackLimits.capacity(StackLimits.BASE_LIMIT, StackLimits.multiplier(wrapper));
            consumer.accept(Component.translatable("screen.tbstackupgrades.current_limit", formatNumber(capacity)));
        }
    }

    private static String format(double multiplier) {
        if (multiplier == Math.floor(multiplier)) {
            return Integer.toString((int) multiplier);
        }
        return Double.toString(multiplier);
    }

    private static String formatNumber(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}
