/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin;

import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tbsu.stackupgrades.util.StackLimitProvider;
import com.tbsu.stackupgrades.util.StackLimits;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Upgrades handler of the backpack. It overrides {@code getSlotLimit} with a
 * constant and therefore never reaches the inherited injection; the upgrade
 * slots must stay at one item per slot, so this override is left untouched.
 *
 * <p>The class only takes part in the stack limit path: the upgrade handler is
 * never bound to a backpack, so the shared code sees a plain handler.</p>
 */
@Mixin(targets = "com.tiviacz.travelersbackpack.inventory.BackpackWrapper$2")
public abstract class UpgradesHandlerMixin implements StackLimitProvider {
    @Unique
    private com.tiviacz.travelersbackpack.inventory.BackpackWrapper tbstackupgrades$wrapper;

    @Override
    public void tbsu$setBackpackWrapper(com.tiviacz.travelersbackpack.inventory.BackpackWrapper wrapper) {
        this.tbstackupgrades$wrapper = wrapper;
    }

    @Override
    public com.tiviacz.travelersbackpack.inventory.BackpackWrapper tbsu$getBackpackWrapper() {
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

    /**
     * Keeps the one-item limit of the upgrade handler even when the backpack
     * reports a multiplier through a menu that shares this handler.
     */
    @Inject(method = "getStackLimit(ILnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true, remap = false)
    private void tbstackupgrades$keepUpgradeSlotLimit(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (this.tbstackupgrades$wrapper == null) {
            return;
        }
        UpgradeManager manager = this.tbstackupgrades$wrapper.getUpgradeManager();
        if (manager != null && StackLimits.multiplier(this.tbstackupgrades$wrapper) > 1.0D) {
            cir.setReturnValue(1);
        }
    }
}


