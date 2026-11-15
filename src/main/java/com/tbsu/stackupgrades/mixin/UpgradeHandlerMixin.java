/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin;

import com.tbsu.stackupgrades.item.StackUpgradeItem;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The upgrade handler refuses any upgrade item whose item is already present in
 * the backpack. That check also gates the capacity the slot reports, because the
 * slot computes it by simulating an insert - so overriding the slot alone left a
 * second copy of one tier unplaceable.
 *
 * <p>Only this addon's stack upgrades skip the duplicate scan.</p>
 */
@Mixin(targets = "com.tiviacz.travelersbackpack.inventory.BackpackWrapper$2")
public abstract class UpgradeHandlerMixin {
    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true, remap = false)
    private void tbstackupgrades$allowDuplicateStackUpgrades(int slot, ItemStack stack,
                                                             CallbackInfoReturnable<Boolean> cir) {
        if (StackUpgradesConfig.allowDuplicateStackUpgrades && !stack.isEmpty()
                && stack.getItem() instanceof StackUpgradeItem) {
            cir.setReturnValue(true);
        }
    }
}
