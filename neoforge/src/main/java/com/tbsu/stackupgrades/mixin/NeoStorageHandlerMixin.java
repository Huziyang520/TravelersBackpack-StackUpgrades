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
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NeoForge storage handler: applies the stack upgrade multiplier to the slot
 * capacity of the backpack's {@code ItemStacksResourceHandler}.
 *
 * <p>NeoForge routes every slot limit and every insertion through
 * {@code getCapacity}, so this single injection covers both. The target is the
 * anonymous handler of {@code BackpackWrapper}, because that one overrides the
 * capacity of the base class.</p>
 */
@Mixin(targets = "com.tiviacz.travelersbackpack.inventory.BackpackWrapper$1")
public abstract class NeoStorageHandlerMixin implements StackLimitProvider {
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
        return ((ItemStacksResourceHandler) (Object) this).size();
    }

    @Override
    public ItemStack tbsu$stack(int index) {
        ItemStacksResourceHandler self = (ItemStacksResourceHandler) (Object) this;
        return self.getResource(index).toStack((int) self.getAmountAsLong(index));
    }

    @Inject(method = "getCapacity(ILnet/neoforged/neoforge/transfer/item/ItemResource;)I",
            at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$getCapacity(int index, ItemResource resource,
                                             CallbackInfoReturnable<Integer> cir) {
        BackpackWrapper wrapper = this.tbstackupgrades$wrapper;
        if (wrapper == null) {
            return;
        }

        double multiplier = StackLimits.multiplier(wrapper);
        if (multiplier <= 1.0D) {
            return;
        }

        int itemMax = resource.isEmpty() ? StackLimits.BASE_LIMIT : resource.getMaxStackSize();
        cir.setReturnValue(StackLimits.capacity(itemMax, multiplier));
    }
}
