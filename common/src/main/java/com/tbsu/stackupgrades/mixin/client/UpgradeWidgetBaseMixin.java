/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime. It is deliberately licensed
 * under LGPL-3.0-or-later so that even the strictest reading of the LGPL
 * (mixin = modification of the library) is satisfied; the MIT option remains
 * available to downstream users. See THIRD-PARTY-NOTICES.md and LICENSE.
 *
 * 本类属于 Mixin 注入层，运行时注入 Traveler's Backpack（LGPL-3.0）与 Minecraft 类。
 * 为满足 LGPL 最严格的解读，本层特意采用 LGPL-3.0-or-later 许可（同时保留 MIT 选项）。
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
 * Clicking the sidebar icon of a stack upgrade normally sends the removal
 * packet. Refuse that locally (and show the warning) while oversized stacks
 * would be left behind; the server refuses it as well.
 */
@Mixin(UpgradeWidgetBase.class)
public abstract class UpgradeWidgetBaseMixin {
    @Inject(method = "removeUpgrade", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$guardUpgradeRemoval(CallbackInfoReturnable<Boolean> cir) {
        UpgradeBase<?> upgrade = ((UpgradeWidgetBase<?>) (Object) this).getUpgrade();
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
