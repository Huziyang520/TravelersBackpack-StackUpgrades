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
import com.tiviacz.travelersbackpack.network.ServerboundActionTagPacket;
import com.tbsu.stackupgrades.client.StackUpgradeWarning;
import com.tbsu.stackupgrades.upgrade.StackUpgrade;
import com.tbsu.stackupgrades.util.UpgradeRemovalGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Clicking the sidebar icon of a stack upgrade removes it right away.
 *
 * <p>This version of the widget toggles an upgrade tab on every icon click
 * without asking whether the upgrade has one, so a passive upgrade without a tab
 * opened an empty, mispositioned tab and only a second click (on the remove
 * button) took the upgrade out. The newer baseline checks
 * {@code UpgradeBase#hasTab()} first and removes the upgrade when there is no
 * tab; this injection restores that behaviour.</p>
 */
@Mixin(UpgradeWidgetBase.class)
public abstract class UpgradeWidgetClickMixin {
    @Inject(method = "m_6375_(DDI)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void tbstackupgrades$removeOnIconClick(double mouseX, double mouseY, int button,
                                                  CallbackInfoReturnable<Boolean> cir) {
        UpgradeWidgetBase<?> self = (UpgradeWidgetBase<?>) (Object) this;
        UpgradeBase<?> upgrade = self.getUpgrade();
        if (!(upgrade instanceof StackUpgrade)) {
            return;
        }
        if (!self.isMouseOverIcon(mouseX, mouseY)) {
            return;
        }
        if (!self.isBackpackOwner()) {
            cir.setReturnValue(Boolean.FALSE);
            return;
        }

        BackpackWrapper wrapper = upgrade.getUpgradeManager() == null
                ? null
                : upgrade.getUpgradeManager().getWrapper();
        int slot = upgrade.getDataHolderSlot();
        if (UpgradeRemovalGuard.blocksRemoval(wrapper, slot)) {
            StackUpgradeWarning.show(UpgradeRemovalGuard.remainingLimit(wrapper, slot));
            cir.setReturnValue(Boolean.FALSE);
            return;
        }

        ServerboundActionTagPacket.create(ServerboundActionTagPacket.REMOVE_UPGRADE, slot);
        ((com.tiviacz.travelersbackpack.client.screens.BackpackScreen) self.screen).playUIClickSound();
        cir.setReturnValue(Boolean.TRUE);
    }
}
