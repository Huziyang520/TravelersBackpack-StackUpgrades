/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import com.tiviacz.travelersbackpack.init.ModDataComponents;
import net.minecraft.core.component.DataComponentType;

/**
 * Resolves the Traveler's Backpack component types whose codec has to be
 * replaced with the oversized-stack tolerant one.
 * <p>
 * Resolution is lazy and defensive: {@code DataComponentType#codec()} can be
 * queried during early bootstrap, before mod initializers ran.
 */
public final class ModComponents {
    private static DataComponentType<?> backpackContainer;
    private static boolean failed;

    private ModComponents() {
    }

    public static boolean isBackpackContainer(Object type) {
        if (backpackContainer == null && !failed) {
            try {
                backpackContainer = ModDataComponents.BACKPACK_CONTAINER;
            } catch (Throwable throwable) {
                failed = true;
            }
        }
        return type != null && type == backpackContainer;
    }
}
