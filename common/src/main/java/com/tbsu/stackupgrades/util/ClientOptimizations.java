/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import com.tbsu.stackupgrades.StackUpgrades;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Client side switches for the two convenience behaviours. Kept in its own file
 * so a server never shares them: they only change what the local player sees.
 */
public final class ClientOptimizations {
    private static final String FILE_NAME = "tbstackupgrades-client.properties";

    /** Shift-clicking into the backpack merges stacks whose max size is not 64. */
    public static boolean quickTransferStacking = true;
    /** The backpack's sort button merges stacks up to the upgraded capacity. */
    public static boolean sorterStacking = true;

    private ClientOptimizations() {
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        Properties properties = new Properties();

        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
            } catch (IOException e) {
                StackUpgrades.LOGGER.warn("Could not read {}, using defaults", path, e);
            }
        }

        quickTransferStacking = Boolean.parseBoolean(properties.getProperty("quickTransferStacking", "true"));
        sorterStacking = Boolean.parseBoolean(properties.getProperty("sorterStacking", "true"));

        properties.setProperty("quickTransferStacking", Boolean.toString(quickTransferStacking));
        properties.setProperty("sorterStacking", Boolean.toString(sorterStacking));

        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                properties.store(out, "Traveler's Backpack: Stack Upgrades - client options");
            }
        } catch (IOException e) {
            StackUpgrades.LOGGER.warn("Could not write config {}", path, e);
        }
    }
}
