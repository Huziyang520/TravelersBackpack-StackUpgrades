/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.client;

/**
 * Client side state for the "cannot remove upgrade" warning. The action bar is
 * not rendered while a container screen is open, so the backpack screen draws a
 * banner above (or below) its GUI instead.
 * <p>
 * The lifetime is wall-clock based: in 1.21+ the screen can be extracted more
 * than once per frame, so counting render calls made the message disappear
 * almost instantly.
 */
public final class StackUpgradeWarning {
    private static final long DURATION_MS = 4000L;

    private static long expiresAt;
    private static int limit;

    private StackUpgradeWarning() {
    }

    public static void show(int remainingLimit) {
        expiresAt = System.currentTimeMillis() + DURATION_MS;
        limit = remainingLimit;
    }

    public static boolean active() {
        return System.currentTimeMillis() < expiresAt;
    }

    public static int limit() {
        return limit;
    }
}
