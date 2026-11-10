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
import com.tbsu.stackupgrades.util.StackLimitProvider;
import com.tbsu.stackupgrades.util.StackLimits;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the stack upgrade multiplier to the backpack storage handler.
 * Only handlers that had a {@link BackpackWrapper} bound by
 * {@link BackpackWrapperMixin} are affected, everything else keeps behaving
 * exactly like before.
 */
@Mixin(ItemStackHandler.class)
public abstract class ItemStackHandlerMixin implements StackLimitProvider {
    @Unique
    private BackpackWrapper tbstackupgrades$wrapper;

    @Override
    public void tbsu$setBackpackWrapper(BackpackWrapper wrapper) {
        this.tbstackupgrades$wrapper = wrapper;
    }

    @Override
    public BackpackWrapper tbsu$getBackpackWrapper() {
        return this.tbstackupgrades$wrapper;
    }

    @Inject(method = "getSlotLimit", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$getSlotLimit(int slot, CallbackInfoReturnable<Integer> cir) {
        BackpackWrapper wrapper = this.tbstackupgrades$wrapper;
        if (wrapper == null) {
            return;
        }

        double multiplier = StackLimits.multiplier(wrapper);
        if (multiplier <= 1.0D) {
            return;
        }

        ItemStackHandler self = (ItemStackHandler) (Object) this;
        if (slot < 0 || slot >= self.getSlots()) {
            return;
        }
        ItemStack stack = self.getStackInSlot(slot);
        cir.setReturnValue(StackLimits.capacity(stack, multiplier));
    }

    @Inject(method = "getStackLimit", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$getStackLimit(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        BackpackWrapper wrapper = this.tbstackupgrades$wrapper;
        if (wrapper == null || stack.isEmpty()) {
            return;
        }

        double multiplier = StackLimits.multiplier(wrapper);
        if (multiplier <= 1.0D) {
            return;
        }

        cir.setReturnValue(StackLimits.capacity(stack, multiplier));
    }
}
