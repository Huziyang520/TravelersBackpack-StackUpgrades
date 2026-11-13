/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import net.minecraft.world.item.ItemStack;

/**
 * Duck interface implemented on the backpack storage handler of each loader.
 * Only the storage handler of a backpack gets a wrapper bound to it, so
 * upgrade/tool/filter handlers keep their original behaviour.
 *
 * <p>The handler class differs per loader (Traveler's Backpack keeps its own on
 * Fabric and uses the NeoForge transfer API otherwise), so the shared code only
 * talks to this interface.</p>
 */
public interface StackLimitProvider {
    void tbsu$setBackpackWrapper(BackpackWrapper wrapper);

    BackpackWrapper tbsu$getBackpackWrapper();

    /** Number of slots of the handler. */
    int tbsu$size();

    /** Current stack in the given slot. */
    ItemStack tbsu$stack(int index);
}
