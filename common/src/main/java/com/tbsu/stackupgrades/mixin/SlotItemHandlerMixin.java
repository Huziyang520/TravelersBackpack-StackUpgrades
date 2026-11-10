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
import com.tiviacz.travelersbackpack.inventory.handler.ItemStackHandler;
import com.tiviacz.travelersbackpack.inventory.menu.slot.SlotItemHandler;
import com.tiviacz.travelersbackpack.inventory.menu.slot.UpgradeLockableSlotItemHandler;
import com.tbsu.stackupgrades.item.StackUpgradeItem;
import com.tbsu.stackupgrades.util.StackLimitProvider;
import com.tbsu.stackupgrades.util.StackLimits;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reports the upgraded capacity of a backpack slot to the vanilla click logic.
 *
 * <p>Traveler's Backpack simulates an insertion capped at the item's vanilla
 * max stack size; with a stack upgrade installed the capacity is higher, so the
 * simulation has to be replaced by the real limit. Returning the capacity (not
 * the free space) matches {@code Slot#getMaxStackSize(ItemStack)} semantics and
 * keeps shift-click merging working.</p>
 */
@Mixin(SlotItemHandler.class)
public abstract class SlotItemHandlerMixin {
    @Shadow
    @Final
    protected int index;

    @Shadow
    public abstract ItemStackHandler getItemHandler();

    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$getMaxStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // Traveler's Backpack rejects an upgrade item that is already installed
        // somewhere in the backpack. This slot override keeps the vanilla click
        // logic willing to place another copy of a stack upgrade.
        if (StackUpgradesConfig.allowDuplicateStackUpgrades
                && (Object) this instanceof UpgradeLockableSlotItemHandler
                && stack.getItem() instanceof StackUpgradeItem) {
            cir.setReturnValue(1);
            return;
        }

        ItemStackHandler handler = this.getItemHandler();
        if (!(handler instanceof StackLimitProvider provider)) {
            return;
        }

        BackpackWrapper wrapper = provider.tbsu$getBackpackWrapper();
        if (wrapper == null) {
            return;
        }

        double multiplier = StackLimits.multiplier(wrapper);
        if (multiplier <= 1.0D) {
            return;
        }

        if (stack.isEmpty() || !handler.isItemValid(this.index, stack)) {
            cir.setReturnValue(0);
            return;
        }

        cir.setReturnValue(StackLimits.capacity(stack, multiplier));
    }
}
