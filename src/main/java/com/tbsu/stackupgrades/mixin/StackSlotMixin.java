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
import net.neoforged.neoforge.items.SlotItemHandler;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Storage slot: exposes the handler behind it to the shared code. The slot limit
 * itself is provided by the handler, since the vanilla click logic asks the slot
 * for {@code getMaxStackSize}.
 */
@Mixin(SlotItemHandler.class)
public abstract class StackSlotMixin implements SlotLimitProvider {
    @Override
    public StackLimitProvider tbsu$limitProvider() {
        Object handler = ((SlotItemHandler) (Object) this).getItemHandler();
        return handler instanceof StackLimitProvider provider ? provider : null;
    }
}
