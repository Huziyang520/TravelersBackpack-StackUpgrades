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
import com.tbsu.stackupgrades.util.StackLimitProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Binds the freshly created storage handler to its backpack so that the stack
 * multiplier can be resolved later on.
 */
@Mixin(targets = "com.tiviacz.travelersbackpack.inventory.BackpackWrapper")
public abstract class BackpackWrapperMixin {
    @Inject(method = "loadStorage()V", at = @At("RETURN"), remap = false)
    private void tbstackupgrades$bindStorageHandler(CallbackInfo ci) {
        BackpackWrapper wrapper = (BackpackWrapper) (Object) this;
        Object storage = wrapper.getStorage();
        if (storage instanceof StackLimitProvider provider) {
            provider.tbsu$setBackpackWrapper(wrapper);
        }
    }
}


