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

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.sorter.ContainerSorter;
import com.tiviacz.travelersbackpack.inventory.sorter.SortSelector;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import com.tbsu.stackupgrades.util.StackLimits;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
    /** Backpack being sorted on this thread; the merge helper has no other way in. */
    @Unique
    private static final ThreadLocal<BackpackWrapper> tbstackupgrades$sorting = new ThreadLocal<>();

    @Inject(method = "sortBackpack", at = @At("HEAD"))
    private static void tbstackupgrades$beginSort(BackpackWrapper wrapper, Player player,
                                                  SortSelector.SortType type, boolean sorting,
                                                  CallbackInfo ci) {
        tbstackupgrades$sorting.set(wrapper);
    }

    @Inject(method = "sortBackpack", at = @At("RETURN"))
    private static void tbstackupgrades$endSort(BackpackWrapper wrapper, Player player,
                                                SortSelector.SortType type, boolean sorting,
                                                CallbackInfo ci) {
        tbstackupgrades$sorting.remove();
    }

    @Inject(method = "quickStackToBackpackNoSort", at = @At("HEAD"))
    private static void tbstackupgrades$beginQuickStack(BackpackWrapper wrapper, Player player, boolean sorting,
                                                        CallbackInfo ci) {
        tbstackupgrades$sorting.set(wrapper);
    }

    @Inject(method = "quickStackToBackpackNoSort", at = @At("RETURN"))
    private static void tbstackupgrades$endQuickStack(BackpackWrapper wrapper, Player player, boolean sorting,
                                                      CallbackInfo ci) {
        tbstackupgrades$sorting.remove();
    }

    @Inject(method = "transferToBackpackNoSort", at = @At("HEAD"))
    private static void tbstackupgrades$beginTransfer(BackpackWrapper wrapper, Player player, boolean sorting,
                                                      CallbackInfo ci) {
        tbstackupgrades$sorting.set(wrapper);
    }

    @Inject(method = "transferToBackpackNoSort", at = @At("RETURN"))
    private static void tbstackupgrades$endTransfer(BackpackWrapper wrapper, Player player, boolean sorting,
                                                    CallbackInfo ci) {
        tbstackupgrades$sorting.remove();
    }

    /**
     * Vanilla merges up to {@code ItemStack#getMaxStackSize()}, so two half stacks
     * of a 64-item never fill a slot that now holds 256, and items whose max size
     * is 1 never merge at all. Merge up to the upgraded capacity instead.
     */
    @Inject(method = "combineStacks", at = @At("HEAD"), cancellable = true)
    private static void tbstackupgrades$combineToCapacity(ItemStack target, ItemStack source, CallbackInfo ci) {
        if (!StackUpgradesConfig.sorterStacking) {
            return;
        }
        BackpackWrapper wrapper = tbstackupgrades$sorting.get();
        if (wrapper == null) {
            return;
        }
        int vanilla = target.getMaxStackSize();
        int capacity = StackLimits.capacity(vanilla, StackLimits.multiplier(wrapper));
        if (capacity <= vanilla) {
            return;
        }
        int total = target.getCount() + source.getCount();
        if (capacity >= total) {
            target.grow(source.getCount());
            source.setCount(0);
            ci.cancel();
            return;
        }
        int moved = capacity - target.getCount();
        if (moved > 0) {
            target.grow(moved);
            source.shrink(moved);
        }
        ci.cancel();
    }
    @Inject(method = "addStackWithMerge", at = @At("HEAD"), cancellable = true)
    private static void tbstackupgrades$keepOversizedStacks(List<ItemStack> stacks, ItemStack newStack, CallbackInfo ci) {
        if (!newStack.isEmpty() && newStack.getCount() > newStack.getMaxStackSize()) {
            stacks.add(newStack);
            ci.cancel();
        }
    }
}
