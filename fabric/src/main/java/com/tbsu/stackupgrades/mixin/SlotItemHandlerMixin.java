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
import com.tiviacz.travelersbackpack.inventory.menu.slot.SlotItemHandler;
import com.tiviacz.travelersbackpack.inventory.menu.slot.UpgradeLockableSlotItemHandler;
import com.tbsu.stackupgrades.item.StackUpgradeItem;
import com.tbsu.stackupgrades.util.SlotLimitProvider;
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
 * Fabric backpack slot: reports the upgraded capacity to the vanilla click
 * logic.
 *
 * <p>Traveler's Backpack simulates an insertion capped at the item's vanilla
 * max stack size; with a stack upgrade installed the capacity is higher, so the
 * simulation has to be replaced by the real limit. Returning the capacity (not
 * the free space) matches {@code Slot#getMaxStackSize(ItemStack)} semantics and
 * keeps shift-click merging working.</p>
 */
@Mixin(SlotItemHandler.class)
public abstract class SlotItemHandlerMixin implements SlotLimitProvider {
    @Shadow
    @Final
    protected int index;

    @Shadow
    public abstract ItemStackHandler getItemHandler();

    @Override
    public StackLimitProvider tbsu$limitProvider() {
        ItemStackHandler handler = this.getItemHandler();
        return handler instanceof StackLimitProvider provider ? provider : null;
    }

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

        StackLimitProvider provider = this.tbsu$limitProvider();
        if (provider == null) {
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

        if (stack.isEmpty() || !this.getItemHandler().isItemValid(this.index, stack)) {
            cir.setReturnValue(0);
            return;
        }

        cir.setReturnValue(StackLimits.capacity(stack, multiplier));
    }
}
