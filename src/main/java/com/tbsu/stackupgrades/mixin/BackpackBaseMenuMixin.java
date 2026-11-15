/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin;

import com.tiviacz.travelersbackpack.inventory.menu.BackpackBaseMenu;
import com.tbsu.stackupgrades.util.ClickLimits;
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
 * Shift-click handling of the backpack menu.
 *
 * <p>The removal guard only reacts to a slot that really holds one of this mod's
 * stack upgrades. Upgrade, tool and filter slots share the lockable slot class,
 * so a bare slot-class check refused ordinary transfers and raised the
 * "cannot remove upgrade" message while items were merely being moved.</p>
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
        if (UpgradeRemovalGuard.blocksRemovalInSlot(self.getWrapper(), slot)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    /** Slot range of the move that currently treats its items as stackable. */
    @Unique
    private static final ThreadLocal<Boolean> tbstackupgrades$stackable = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(method = "moveItemStackTo", at = @At("HEAD"))
    private void tbstackupgrades$markRange(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection,
                                           CallbackInfoReturnable<Boolean> cir) {
        tbstackupgrades$stackable.set(StackUpgradesConfig.quickTransferStacking
                && !stack.isEmpty()
                && StackLimits.hasUpgradedSlot((BackpackBaseMenu) (Object) this, startIndex, endIndex));
        ClickLimits.beginMove(stack);
    }

    @Inject(method = "moveItemStackTo", at = @At("RETURN"))
    private void tbstackupgrades$clearRange(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection,
                                            CallbackInfoReturnable<Boolean> cir) {
        tbstackupgrades$stackable.remove();
        ClickLimits.endMove();
    }

    @Redirect(method = "moveItemStackTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isStackable()Z"))
    private boolean tbstackupgrades$treatAsStackable(ItemStack stack) {
        return stack.isStackable() || tbstackupgrades$stackable.get();
    }

    /** Caps how much one shift click may move in total, see {@link ClickLimits#allowance}. */
    @Redirect(method = "moveItemStackTo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I"))
    private int tbstackupgrades$capTransfer(Slot slot, ItemStack stack) {
        return ClickLimits.allowance(slot.getMaxStackSize(stack), stack);
    }
}

