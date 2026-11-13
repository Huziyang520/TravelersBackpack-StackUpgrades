/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-independent identity of the mod.
 *
 * <p>Everything that binds to a loader (entrypoints, item registration, config
 * registration, creative tab, loot) lives in the loader modules: the same
 * sources are compiled once per loader, so this class stays free of loader
 * imports.</p>
 */
public final class StackUpgrades {
    public static final String MOD_ID = "tbstackupgrades";
    public static final Logger LOGGER = LoggerFactory.getLogger("Traveler'sBackpack-StackUpgrades");

    private StackUpgrades() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
