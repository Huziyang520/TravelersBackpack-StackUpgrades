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
 * the backpack. That check is not only a placement rule: {@code SlotItemHandler}
 * computes its capacity by simulating an insert, so the refusal makes the slot
 * report a capacity of 0 and the vanilla click logic places nothing. Overriding
 * {@code mayPlace} on the slot therefore never allowed a second copy of one tier.
 *
 * <p>Only this addon's stack upgrades skip the duplicate scan; the tank capacity
 * and feature-flag checks are skipped with it, both of which are always
 * satisfied for a stack upgrade.</p>
 */
@Mixin(targets = "com.tiviacz.travelersbackpack.inventory.BackpackWrapper$2")
public abstract class UpgradeHandlerMixin {
    @Inject(method = "isItemValid(ILnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void tbstackupgrades$allowDuplicateStackUpgrades(int slot, ItemStack stack,
                                                             CallbackInfoReturnable<Boolean> cir) {
        if (StackUpgradesConfig.allowDuplicateStackUpgrades && !stack.isEmpty()
                && stack.getItem() instanceof StackUpgradeItem) {
            cir.setReturnValue(true);
        }
    }
}
