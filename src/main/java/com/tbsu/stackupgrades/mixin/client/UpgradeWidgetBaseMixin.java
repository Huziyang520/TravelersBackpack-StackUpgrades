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
import com.tbsu.stackupgrades.client.StackUpgradeWarning;
import com.tbsu.stackupgrades.util.UpgradeRemovalGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Clicking the sidebar remove button of a stack upgrade normally sends the
 * removal packet. Refuse that locally (and show the warning) while oversized
 * stacks would be left behind; the server refuses it as well.
 *
 * <p>This version of the widget has no dedicated {@code removeUpgrade} method -
 * the removal is inline in {@code removeButtonMouseClicked} - so the guard sits
 * there.</p>
 */
@Mixin(UpgradeWidgetBase.class)
public abstract class UpgradeWidgetBaseMixin {
    @Inject(method = "removeButtonMouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void tbstackupgrades$guardUpgradeRemoval(double mouseX, double mouseY, int button,
                                                     CallbackInfoReturnable<Boolean> cir) {
        UpgradeWidgetBase<?> self = (UpgradeWidgetBase<?>) (Object) this;
        // The widget is asked about every click on the screen, so the same
        // preconditions the original method uses have to be checked first -
        // otherwise an unrelated click reports a blocked removal.
        if (!self.isTabOpened() || !self.isMouseOverRemoveButton(mouseX, mouseY)) {
            return;
        }

        UpgradeBase<?> upgrade = self.getUpgrade();
        if (upgrade == null || upgrade.getUpgradeManager() == null) {
            return;
        }

        BackpackWrapper wrapper = upgrade.getUpgradeManager().getWrapper();
        int slot = upgrade.getDataHolderSlot();
        if (UpgradeRemovalGuard.blocksRemoval(wrapper, slot)) {
            StackUpgradeWarning.show(UpgradeRemovalGuard.remainingLimit(wrapper, slot));
            cir.setReturnValue(false);
        }
    }
}
