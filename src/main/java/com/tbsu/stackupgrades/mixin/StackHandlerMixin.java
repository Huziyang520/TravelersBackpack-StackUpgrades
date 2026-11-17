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
import com.tbsu.stackupgrades.util.StackLimitProvider;
import com.tbsu.stackupgrades.util.StackLimits;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forge storage handler: applies the stack upgrade multiplier to the slot
 * capacity.
 *
 * <p>Only handlers that had a {@link BackpackWrapper} bound by
 * {@code BackpackWrapperMixin} are affected; the upgrade and tool handlers keep
 * their original behaviour. Both the slot limit (which the slot reports to the
 * vanilla click logic) and the stack limit (which {@code insertItem} uses) are
 * raised.</p>
 */
@Mixin(targets = "net.minecraftforge.items.ItemStackHandler")
public abstract class StackHandlerMixin implements StackLimitProvider {
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

    @Inject(method = "getSlotLimit(I)I", at = @At("HEAD"), cancellable = true, remap = false)
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
        cir.setReturnValue(StackLimits.capacity(self.getStackInSlot(slot), multiplier));
    }

    @Inject(method = "getStackLimit(ILnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true, remap = false)
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

    /**
     * Extraction is capped at the item's own max stack size, so anything that
     * already moved an oversized stack into its own container could only get one
     * vanilla stack removed here and the rest stayed in the slot.
     */
    @Redirect(method = "extractItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    private int tbstackupgrades$extractLimit(ItemStack stack) {
        int itemMax = stack.getMaxStackSize();
        BackpackWrapper wrapper = this.tbstackupgrades$wrapper;
        if (wrapper == null) {
            return itemMax;
        }

        double multiplier = StackLimits.multiplier(wrapper);
        if (multiplier <= 1.0D) {
            return itemMax;
        }

        return Math.max(itemMax, StackLimits.capacity(stack, multiplier));
    }
}




