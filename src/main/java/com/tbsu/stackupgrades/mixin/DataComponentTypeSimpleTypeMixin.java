/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin;

import com.mojang.serialization.Codec;
import com.tbsu.stackupgrades.util.ContainerCodecs;
import com.tbsu.stackupgrades.util.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Swaps the persistent codec of Traveler's Backpack's storage component for the
 * oversized-stack tolerant variant, so that slots holding more than 99 items
 * survive a world save.
 */
@Mixin(targets = "net.minecraft.core.component.DataComponentType$Builder$SimpleType")
public abstract class DataComponentTypeSimpleTypeMixin {
    @Inject(method = "codec", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$lenientCodec(CallbackInfoReturnable<Codec<?>> cir) {
        if (ModComponents.isBackpackContainer(this)) {
            cir.setReturnValue(ContainerCodecs.CONTAINER_CODEC);
        }
    }
}
