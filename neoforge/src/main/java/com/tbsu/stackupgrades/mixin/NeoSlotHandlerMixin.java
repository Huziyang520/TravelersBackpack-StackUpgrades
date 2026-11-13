/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin;

import com.tbsu.stackupgrades.util.SlotLimitProvider;
import com.tbsu.stackupgrades.util.StackLimitProvider;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.spongepowered.asm.mixin.Mixin;

/**
 * NeoForge storage slot: exposes the handler behind it to the shared code.
 * The slot limit itself is provided by {@code NeoStorageHandlerMixin}, since
 * NeoForge asks the handler for the capacity.
 */
@Mixin(ResourceHandlerSlot.class)
public abstract class NeoSlotHandlerMixin implements SlotLimitProvider {
    @Override
    public StackLimitProvider tbsu$limitProvider() {
        Object handler = ((ResourceHandlerSlot) (Object) this).getResourceHandler();
        return handler instanceof StackLimitProvider provider ? provider : null;
    }
}
