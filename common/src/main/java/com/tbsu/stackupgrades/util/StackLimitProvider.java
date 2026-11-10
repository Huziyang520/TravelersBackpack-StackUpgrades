/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;

/**
 * Duck interface implemented by Traveler's Backpack's {@code ItemStackHandler}
 * through a mixin. Only the storage handler of a backpack gets a wrapper bound
 * to it, so upgrade/tool/filter handlers keep their original behaviour.
 */
public interface StackLimitProvider {
    void tbsu$setBackpackWrapper(BackpackWrapper wrapper);

    BackpackWrapper tbsu$getBackpackWrapper();
}
