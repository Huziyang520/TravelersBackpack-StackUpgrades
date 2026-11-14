/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin;

import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item stacks store their count in a single byte, so anything above 127 would be
 * written as a negative number and come back as a different amount - an
 * oversized backpack slot would silently lose items on world save.
 *
 * <p>The count is therefore clamped to the byte range and the real amount is kept
 * in a second key that only this mod reads. Stacks that fit into a byte are
 * written exactly like vanilla, so nothing else sees a difference.</p>
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "save", at = @At("TAIL"))
    private void tbstackupgrades$saveOversizedCount(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        ItemStack self = (ItemStack) (Object) this;
        int count = self.getCount();
        if (count <= 127) {
            return;
        }
        tag.putByte("Count", (byte) 127);
        tag.putInt("tbstackupgrades:count", Math.min(count, StackUpgradesConfig.HARD_MAX_SLOT_LIMIT));
    }

    @Inject(method = "of", at = @At("RETURN"), cancellable = true)
    private static void tbstackupgrades$readOversizedCount(CompoundTag tag, CallbackInfoReturnable<ItemStack> cir) {
        if (tag == null || !tag.contains("tbstackupgrades:count")) {
            return;
        }
        ItemStack stack = cir.getReturnValue();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int count = tag.getInt("tbstackupgrades:count");
        if (count > 127) {
            stack.setCount(Math.min(count, StackUpgradesConfig.HARD_MAX_SLOT_LIMIT));
            cir.setReturnValue(stack);
        }
    }
}
