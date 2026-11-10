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
import com.tbsu.stackupgrades.util.UpgradeRemovalGuard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
            UpgradeRemovalGuard.notifyBlocked(player, UpgradeRemovalGuard.remainingLimit(self.getWrapper(), slot.index));
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
