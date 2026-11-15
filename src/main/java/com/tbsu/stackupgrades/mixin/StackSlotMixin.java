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
import net.minecraftforge.items.SlotItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Backpack slot: exposes the handler to the shared code and reports the upgraded
 * capacity to the vanilla click logic.
 *
 * <p>{@code SlotItemHandler} overrides both {@code getMaxStackSize} overloads and
 * caps the item overload at {@code min(itemMax, slotLimit)}, so an injection on
 * the vanilla {@code Slot} never runs for a backpack slot, and raising the handler
 * limit alone still left every click at 64 items. Both overloads have to be
 * replaced on the loader's slot class itself.</p>
 *
 * <p>The target is a Forge class, which is not part of the Minecraft mappings, so
 * its runtime (SRG) names are addressed directly and left unmapped.</p>
 */
@Mixin(targets = "net.minecraftforge.items.SlotItemHandler")
public abstract class StackSlotMixin implements SlotLimitProvider {
    @Override
    public StackLimitProvider tbsu$limitProvider() {
        Object handler = ((SlotItemHandler) (Object) this).getItemHandler();
        return handler instanceof StackLimitProvider provider ? provider : null;
    }

    @Inject(method = "m_5866_(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void tbstackupgrades$getMaxStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int capacity = tbstackupgrades$capacity(stack);
        if (capacity > 0) {
            cir.setReturnValue(capacity);
        }
    }

    @Inject(method = "m_6641_()I", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
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
