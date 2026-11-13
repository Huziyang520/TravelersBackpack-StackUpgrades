/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tbsu.stackupgrades.upgrade.StackUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Prevents removing a stack upgrade while the backpack still holds stacks that
 * would exceed the lower limit. Without this the player could fill a slot with
 * 1024 items, pull the upgrade out and keep the huge stack for free.
 * The message mirrors Sophisticated Backpacks' "at least one slot needs a
 * higher stack limit" warning.
 */
public final class UpgradeRemovalGuard {
    private UpgradeRemovalGuard() {
    }

    /**
     * @param dataHolderSlot index of the upgrade slot that is about to be emptied
     * @return true if removing that upgrade has to be denied
     */
    public static boolean blocksRemoval(BackpackWrapper wrapper, int dataHolderSlot) {
        StackUpgrade target = findUpgrade(wrapper, dataHolderSlot);
        if (target == null) {
            return false;
        }

        Object storage = wrapper.getStorage();
        if (!(storage instanceof StackLimitProvider provider)) {
            return false;
        }

        double remaining = StackLimits.multiplier(wrapper) / target.getMultiplier();
        for (int i = 0; i < provider.tbsu$size(); i++) {
            ItemStack stack = provider.tbsu$stack(i);
            if (!stack.isEmpty() && stack.getCount() > StackLimits.capacity(stack, remaining)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Per-slot limit that would remain after removing the upgrade in the given slot.
     */
    public static int remainingLimit(BackpackWrapper wrapper, int dataHolderSlot) {
        StackUpgrade target = findUpgrade(wrapper, dataHolderSlot);
        double remaining = target == null ? 1.0D : StackLimits.multiplier(wrapper) / target.getMultiplier();
        return StackLimits.capacity(StackLimits.BASE_LIMIT, remaining);
    }

    public static void notifyBlocked(Player player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }
        player.sendOverlayMessage(
                Component.translatable("message.tbstackupgrades.cannot_remove_upgrade")
                        .withStyle(ChatFormatting.RED));
    }

    private static StackUpgrade findUpgrade(BackpackWrapper wrapper, int dataHolderSlot) {
        if (wrapper == null) {
            return null;
        }
        UpgradeManager manager = wrapper.getUpgradeManager();
        if (manager == null || manager.upgrades == null) {
            return null;
        }
        for (UpgradeBase<?> upgrade : manager.upgrades) {
            if (upgrade instanceof StackUpgrade stackUpgrade && stackUpgrade.getDataHolderSlot() == dataHolderSlot) {
                return stackUpgrade;
            }
        }
        return null;
    }
}
