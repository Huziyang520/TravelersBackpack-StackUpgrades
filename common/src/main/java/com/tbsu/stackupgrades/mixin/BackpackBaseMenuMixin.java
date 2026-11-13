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

import com.tiviacz.travelersbackpack.inventory.menu.BackpackBaseMenu;
import com.tiviacz.travelersbackpack.inventory.menu.slot.UpgradeLockableSlotItemHandler;
import com.tbsu.stackupgrades.util.StackLimits;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import com.tbsu.stackupgrades.util.UpgradeRemovalGuard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shift-clicking an upgrade slot does not go through {@code mayPickup}, so the
 * same guard has to be applied to the quick-move path.
 */
@Mixin(BackpackBaseMenu.class)
public abstract class BackpackBaseMenuMixin {
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$guardQuickMove(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        BackpackBaseMenu self = (BackpackBaseMenu) (Object) this;
        if (index < 0 || index >= self.slots.size()) {
            return;
        }

        Slot slot = self.slots.get(index);
        if (!(slot instanceof UpgradeLockableSlotItemHandler lockable) || lockable.isHidden) {
            return;
        }

        if (UpgradeRemovalGuard.blocksRemoval(self.getWrapper(), slot.index)) {
            UpgradeRemovalGuard.notifyBlocked(player);
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    /** Slot range of the move that currently treats its items as stackable. */
    @Unique
    private static final ThreadLocal<Boolean> tbstackupgrades$stackable = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** Items left behind by the shift-click cap of the current transfer. */
    @Unique
    private static final ThreadLocal<ItemStack> tbstackupgrades$capped = ThreadLocal.withInitial(() -> ItemStack.EMPTY);
    @Unique
    private static final ThreadLocal<ItemStack> tbstackupgrades$original = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

    /**
     * Shift-click into the backpack merges items whose max stack size is not 64:
     * the menu copies {@code moveItemStackTo} from the base class, so the same
     * treatment has to be applied here.
     */
    @Inject(method = "moveItemStackTo", at = @At("HEAD"))
    private void tbstackupgrades$markRange(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection,
                                           CallbackInfoReturnable<Boolean> cir) {
        tbstackupgrades$stackable.set(StackUpgradesConfig.quickTransferStacking
                && !stack.isEmpty()
                && StackLimits.hasUpgradedSlot((BackpackBaseMenu) (Object) this, startIndex, endIndex));
    }

    @Inject(method = "moveItemStackTo", at = @At("RETURN"))
    private void tbstackupgrades$clearRange(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection,
                                            CallbackInfoReturnable<Boolean> cir) {
        tbstackupgrades$stackable.remove();
    }

    @Redirect(method = "moveItemStackTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isStackable()Z"))
    private boolean tbstackupgrades$treatAsStackable(ItemStack stack) {
        return stack.isStackable() || tbstackupgrades$stackable.get();
    }

    /**
     * Shift-click out of an oversized slot moves the whole stack by default,
     * which floods the target inventory. With the option on, only the item's own
     * max stack size is moved and the rest stays in the slot.
     */
    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void tbstackupgrades$capShiftTransfer(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        BackpackBaseMenu self = (BackpackBaseMenu) (Object) this;
        if (!StackUpgradesConfig.shiftTransferLimit || index < 0 || index >= self.slots.size()) {
            return;
        }

        Slot slot = self.slots.get(index);
        ItemStack stack = slot.getItem();
        int max = stack.getMaxStackSize();
        if (stack.getCount() <= max) {
            return;
        }

        tbstackupgrades$original.set(stack.copy());
        tbstackupgrades$capped.set(stack.copyWithCount(stack.getCount() - max));
        slot.set(stack.copyWithCount(max));
    }

    @Inject(method = "quickMoveStack", at = @At("RETURN"))
    private void tbstackupgrades$restoreCapped(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack capped = tbstackupgrades$capped.get();
        ItemStack original = tbstackupgrades$original.get();
        tbstackupgrades$capped.remove();
        tbstackupgrades$original.remove();
        BackpackBaseMenu self = (BackpackBaseMenu) (Object) this;
        if (capped.isEmpty() || index < 0 || index >= self.slots.size()) {
            return;
        }

        Slot slot = self.slots.get(index);
        ItemStack rest = slot.getItem();
        int remaining = capped.getCount() + (rest.isEmpty() ? 0 : rest.getCount());
        slot.set(original.copyWithCount(remaining));
        slot.setChanged();

        // The click handler repeats the transfer while the returned stack is not
        // empty, so an empty result is what actually stops it after one stack.
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
