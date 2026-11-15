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
import com.tbsu.stackupgrades.util.SlotLimitProvider;
import com.tbsu.stackupgrades.util.StackLimitProvider;
import com.tbsu.stackupgrades.util.StackLimits;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Backpack slot on NeoForge: exposes the handler to the shared code and reports
 * the upgraded capacity to the vanilla click logic.
 *
 * <p>NeoForge's slot caps the reported limit at the item's own max stack size
 * ({@code min(itemMax, handlerSlotLimit)}), so raising the handler limit alone
 * never let a left-click merge past 64 - only the sorter worked. The capacity has
 * to be returned from the slot as well.</p>
 */
@Mixin(SlotItemHandler.class)
public abstract class StackSlotMixin implements SlotLimitProvider {
    @Override
    public StackLimitProvider tbsu$limitProvider() {
        Object handler = ((SlotItemHandler) (Object) this).getItemHandler();
        return handler instanceof StackLimitProvider provider ? provider : null;
    }

    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$getMaxStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int capacity = tbstackupgrades$capacity(stack);
        if (capacity > 0) {
            cir.setReturnValue(capacity);
        }
    }

    @Inject(method = "getMaxStackSize()I", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$getMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        int capacity = tbstackupgrades$capacity(((SlotItemHandler) (Object) this).getItem());
        if (capacity > 0) {
            cir.setReturnValue(capacity);
        }
    }

    /** @return the upgraded capacity, or 0 when this slot is not an upgraded backpack slot. */
    private int tbstackupgrades$capacity(ItemStack stack) {
        StackLimitProvider provider = this.tbsu$limitProvider();
        if (provider == null) {
            return 0;
        }
        BackpackWrapper wrapper = provider.tbsu$getBackpackWrapper();
        if (wrapper == null) {
            return 0;
        }
        double multiplier = StackLimits.multiplier(wrapper);
        if (multiplier <= 1.0D) {
            return 0;
        }
        return StackLimits.capacity(stack, multiplier);
    }
}
