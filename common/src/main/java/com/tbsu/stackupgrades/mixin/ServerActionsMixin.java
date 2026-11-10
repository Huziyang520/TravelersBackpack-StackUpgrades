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

import com.tiviacz.travelersbackpack.common.ServerActions;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.menu.BackpackBaseMenu;
import com.tbsu.stackupgrades.util.UpgradeRemovalGuard;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Authoritative guard for removing an upgrade: the upgrade tab / sidebar icon
 * sends a packet that ends up here, so this is the place that must refuse.
 */
@Mixin(ServerActions.class)
public abstract class ServerActionsMixin {
    @Inject(method = "removeBackpackUpgrade", at = @At("HEAD"), cancellable = true)
    private static void tbstackupgrades$guardUpgradeRemoval(ServerPlayer player, int slot, CallbackInfo ci) {
        if (!(player.containerMenu instanceof BackpackBaseMenu menu)) {
            return;
        }

        BackpackWrapper wrapper = menu.getWrapper();
        if (UpgradeRemovalGuard.blocksRemoval(wrapper, slot)) {
            UpgradeRemovalGuard.notifyBlocked(player);
            ci.cancel();
        }
    }
}
