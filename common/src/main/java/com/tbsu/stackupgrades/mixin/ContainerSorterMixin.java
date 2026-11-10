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

package com.tbsu.stackupgrades.mixin;

import com.tiviacz.travelersbackpack.inventory.sorter.ContainerSorter;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Traveler's Backpack's sorter assumes that a stack never holds more items than
 * {@code ItemStack#getMaxStackSize()} and would otherwise redistribute (and
 * partially shrink) oversized stacks. Stacks above their vanilla limit are
 * therefore passed through untouched; they keep their own slot.
 */
@Mixin(ContainerSorter.class)
public class ContainerSorterMixin {
    @Inject(method = "addStackWithMerge", at = @At("HEAD"), cancellable = true)
    private static void tbstackupgrades$keepOversizedStacks(List<ItemStack> stacks, ItemStack newStack, CallbackInfo ci) {
        if (!newStack.isEmpty() && newStack.getCount() > newStack.getMaxStackSize()) {
            stacks.add(newStack);
            ci.cancel();
        }
    }
}
