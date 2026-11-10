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

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.handler.ItemStackHandler;
import com.tbsu.stackupgrades.util.StackLimitProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Binds the freshly created storage handler to its backpack so that the stack
 * multiplier can be resolved later on.
 */
@Mixin(BackpackWrapper.class)
public abstract class BackpackWrapperMixin {
    @Inject(method = "loadStorage", at = @At("RETURN"))
    private void tbstackupgrades$bindStorageHandler(CallbackInfo ci) {
        BackpackWrapper wrapper = (BackpackWrapper) (Object) this;
        ItemStackHandler storage = wrapper.getStorage();
        if (storage instanceof StackLimitProvider provider) {
            provider.tbsu$setBackpackWrapper(wrapper);
        }
    }
}
