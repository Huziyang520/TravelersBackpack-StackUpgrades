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

import com.tbsu.stackupgrades.util.ContainerCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code ItemStackTemplate#create()} validates the resulting stack with
 * {@code ItemStack#validateStrict}, which rejects every count above the item's
 * stack size and silently returns an empty stack. That would delete oversized
 * stacks whenever a backpack is re-opened, since Traveler's Backpack reloads its
 * contents from the container component on each access.
 */
@Mixin(ItemStackTemplate.class)
public abstract class ItemStackTemplateMixin {
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$createOversized(CallbackInfoReturnable<ItemStack> cir) {
        ItemStackTemplate template = (ItemStackTemplate) (Object) this;
        if (template.count() > template.item().value().getDefaultMaxStackSize()) {
            cir.setReturnValue(ContainerCodecs.createStack(template));
        }
    }
}
