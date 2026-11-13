/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
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
 * Fabric storage handler: applies the stack upgrade multiplier to the slot
 * limit. Only handlers that had a {@link BackpackWrapper} bound by
 * {@code BackpackWrapperMixin} are affected, everything else keeps behaving
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

    @Override
    public int tbsu$size() {
        return ((ItemStackHandler) (Object) this).getSlots();
    }

    @Override
    public ItemStack tbsu$stack(int index) {
        return ((ItemStackHandler) (Object) this).getStackInSlot(index);
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
