/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin.client;

import com.tiviacz.travelersbackpack.client.screens.widgets.UpgradeWidgetBase;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tbsu.stackupgrades.upgrade.StackTier;
import com.tbsu.stackupgrades.upgrade.StackUpgrade;
import com.tbsu.stackupgrades.util.StackLimits;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Adds the multiplier and the resulting slot limit to the tooltip of a stack
 * upgrade icon. This version of the widget has no hook for extra tooltip lines,
 * so the list handed to the renderer is extended instead.
 */
@Mixin(UpgradeWidgetBase.class)
public abstract class UpgradeWidgetTooltipMixin {
    @Redirect(method = "renderTooltip",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"))
    private void tbstackupgrades$addUpgradeLines(GuiGraphics graphics, Font font, List<Component> lines,
                                                 Optional<TooltipComponent> image, int x, int y) {
        List<Component> extended = new ArrayList<>(lines);
        UpgradeBase<?> upgrade = ((UpgradeWidgetBase<?>) (Object) this).getUpgrade();
        if (upgrade instanceof StackUpgrade stackUpgrade) {
            StackTier tier = stackUpgrade.getTier();
            extended.add(Component.translatable("screen.tbstackupgrades.multiplier", format(tier.multiplier())));
            extended.add(Component.translatable("screen.tbstackupgrades.slot_limit", formatNumber(tier.capacityFor64())));

            // with duplicate upgrades the tier multiplier alone says nothing about
            // the real capacity, so show the combined value as well
            BackpackWrapper wrapper = stackUpgrade.getUpgradeManager() == null
                    ? null
                    : stackUpgrade.getUpgradeManager().getWrapper();
            if (wrapper != null) {
                int capacity = StackLimits.capacity(StackLimits.BASE_LIMIT, StackLimits.multiplier(wrapper));
                extended.add(Component.translatable("screen.tbstackupgrades.current_limit", formatNumber(capacity)));
            }
        }
        graphics.renderTooltip(font, extended, image, x, y);
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


