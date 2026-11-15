/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin;

import com.tiviacz.travelersbackpack.inventory.menu.slot.UpgradeLockableSlotItemHandler;
import com.tbsu.stackupgrades.item.StackUpgradeItem;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import com.tbsu.stackupgrades.util.UpgradeRemovalGuard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Denies emptying a stack upgrade slot while the backpack still contains
 * stacks that only fit because of that upgrade.
 * <p>
 * Also lets the same stack upgrade enter another upgrade slot: Traveler's
 * Backpack refuses any upgrade item that is already present in the backpack,
 * which would block installing several copies of one tier.
 *
 * <p>The pickup guard only reacts while the slot really holds one of this mod's
 * stack upgrades: this class is also the base of tool and filter slots, and a
 * bare slot-class check refused ordinary transfers.</p>
 */
@Mixin(UpgradeLockableSlotItemHandler.class)
public abstract class UpgradeLockableSlotItemHandlerMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$allowDuplicatePlacement(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (StackUpgradesConfig.allowDuplicateStackUpgrades && stack.getItem() instanceof StackUpgradeItem) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$guardUpgradeRemoval(Player player, CallbackInfoReturnable<Boolean> cir) {
        UpgradeLockableSlotItemHandler self = (UpgradeLockableSlotItemHandler) (Object) this;
        if (self.isHidden || self.menu == null) {
            return;
        }
        // The message is left to the screen guard and the removal packet: this
        // method also runs for clicks the client already refused, and showing it
        // here printed the warning twice for one attempt.
        if (UpgradeRemovalGuard.blocksRemovalInSlot(self.menu.getWrapper(), self)) {
            cir.setReturnValue(false);
        }
    }
}

