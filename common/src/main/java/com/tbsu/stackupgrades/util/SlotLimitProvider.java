/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

/**
 * Duck interface implemented on the slot classes that wrap a backpack storage
 * handler. The slot class differs per loader, the shared code only needs to get
 * from a {@code Slot} to the handler's limits.
 */
public interface SlotLimitProvider {
    /** The handler behind this slot, or {@code null} when it is not a storage slot. */
    StackLimitProvider tbsu$limitProvider();
}
