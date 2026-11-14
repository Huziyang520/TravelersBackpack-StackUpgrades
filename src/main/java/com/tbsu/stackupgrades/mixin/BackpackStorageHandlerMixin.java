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
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Traveler's Backpack builds its storage handlers as anonymous subclasses of
 * {@code ItemStackHandler} that override {@code getSlotLimit} with a constant, so
 * the inherited injection never runs. The handlers are targeted by name because
 * the override has to be intercepted where it is declared.
 *
 * <p>{@code BackpackWrapper#loadStorage} binds the wrapper to the storage handler
 * only, so the upgrade and tool handlers stay untouched. The upgrade handler
 * ({@code BackpackWrapper$2}) keeps its one-item slot limit on purpose.</p>
 */
@Mixin(targets = "com.tiviacz.travelersbackpack.inventory.BackpackWrapper$1")

public abstract class BackpackStorageHandlerMixin implements StackLimitProvider {
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

        int slots = ((ItemStackHandler) (Object) this).getSlots();
        if (slot < 0 || slot >= slots) {
            return;
        }
        cir.setReturnValue(StackLimits.capacity(((ItemStackHandler) (Object) this).getStackInSlot(slot), multiplier));
    }
}

