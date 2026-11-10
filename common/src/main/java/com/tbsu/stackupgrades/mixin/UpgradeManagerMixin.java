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

import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tiviacz.travelersbackpack.item.upgrade.UpgradeItem;
import com.tbsu.stackupgrades.item.StackUpgradeItem;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Traveler's Backpack allows exactly one upgrade per upgrade class. Every stack
 * tier uses its own class, but duplicates of the <em>same</em> tier are refused
 * by that check as well. This mixin only relaxes the rule for this addon's
 * stack upgrades, so a backpack can hold e.g. five tier 4 upgrades that all
 * multiply the slot capacity (16^5 = 1,048,576x, capped by the config).
 * <p>
 * Because the duplicate check is now permissive, {@code applyUpgrade} has to
 * stay idempotent per slot: Traveler's Backpack calls it again every time the
 * upgrade handler reports a change (e.g. when a menu is opened), which would
 * otherwise register a second upgrade for the same slot and silently multiply
 * the capacity again.
 */
@Mixin(UpgradeManager.class)
public abstract class UpgradeManagerMixin {
    @Inject(method = "canAddUpgrade", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$allowDuplicateStackUpgrades(UpgradeItem upgradeItem, CallbackInfoReturnable<Boolean> cir) {
        if (StackUpgradesConfig.allowDuplicateStackUpgrades && upgradeItem instanceof StackUpgradeItem) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "applyUpgrade", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$skipAlreadyAppliedSlot(int slot, CallbackInfoReturnable<Boolean> cir) {
        UpgradeManager self = (UpgradeManager) (Object) this;
        Optional<UpgradeBase<?>> existing = self.mappedUpgrades.get(slot);
        if (existing != null && existing.isPresent()) {
            cir.setReturnValue(false);
        }
    }
}
