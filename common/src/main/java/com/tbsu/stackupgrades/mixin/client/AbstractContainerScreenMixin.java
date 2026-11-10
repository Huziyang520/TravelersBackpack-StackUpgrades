/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime. It is deliberately licensed
 * under LGPL-3.0-or-later so that even the strictest reading of the LGPL
 * (mixin = modification of the library) is satisfied; the MIT option remains
 * available to downstream users. See THIRD-PARTY-NOTICES.md and LICENSE.
 *
 * 本类属于 Mixin 注入层，运行时注入 Traveler's Backpack（LGPL-3.0）与 Minecraft 类。
 * 为满足 LGPL 最严格的解读，本层特意采用 LGPL-3.0-or-later 许可（同时保留 MIT 选项）。
 */

package com.tbsu.stackupgrades.mixin.client;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.handler.ItemStackHandler;
import com.tiviacz.travelersbackpack.inventory.menu.slot.SlotItemHandler;
import com.tiviacz.travelersbackpack.inventory.menu.slot.UpgradeLockableSlotItemHandler;
import com.tbsu.stackupgrades.client.StackUpgradeWarning;
import com.tbsu.stackupgrades.util.StackLimitProvider;
import com.tbsu.stackupgrades.util.StackLimits;
import com.tbsu.stackupgrades.util.UpgradeRemovalGuard;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Client side additions for backpack screens:
 * <ul>
 *     <li>adds a "amount / capacity" line to the tooltip of backpack slots, mirroring
 *     Sophisticated Backpacks;</li>
 *     <li>refuses clicks on an upgrade slot while oversized stacks would be left
 *     behind, and shows the warning as a red tooltip (the action bar is not
 *     rendered while a container screen is open).</li>
 * </ul>
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
    private void tbstackupgrades$addCapacityLine(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        Slot slot = this.hoveredSlot;
        if (slot == null || stack.isEmpty() || !(slot instanceof SlotItemHandler slotItemHandler)) {
            return;
        }

        ItemStackHandler handler = slotItemHandler.getItemHandler();
        if (!(handler instanceof StackLimitProvider provider)) {
            return;
        }

        BackpackWrapper wrapper = provider.tbsu$getBackpackWrapper();
        if (wrapper == null) {
            return;
        }

        double multiplier = StackLimits.multiplier(wrapper);
        if (multiplier <= 1.0D) {
            return;
        }

        int capacity = StackLimits.capacity(stack, multiplier);
        if (capacity <= stack.getMaxStackSize()) {
            return;
        }

        List<Component> tooltip = new ArrayList<>(cir.getReturnValue());
        tooltip.add(Component.translatable("tooltip.tbstackupgrades.capacity",
                        format(stack.getCount()), format(capacity))
                .withStyle(ChatFormatting.DARK_AQUA));
        cir.setReturnValue(tooltip);
    }

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$guardUpgradeSlotClick(Slot slot, int slotId, int buttonNum,
                                                       net.minecraft.world.inventory.ContainerInput containerInput,
                                                       CallbackInfo ci) {
        if (containerInput != net.minecraft.world.inventory.ContainerInput.PICKUP
                && containerInput != net.minecraft.world.inventory.ContainerInput.QUICK_MOVE) {
            return;
        }
        if (!(slot instanceof UpgradeLockableSlotItemHandler lockable) || lockable.isHidden) {
            return;
        }

        BackpackWrapper wrapper = lockable.menu == null ? null : lockable.menu.getWrapper();
        int index = slot.getContainerSlot();
        if (UpgradeRemovalGuard.blocksRemoval(wrapper, index)) {
            StackUpgradeWarning.show(UpgradeRemovalGuard.remainingLimit(wrapper, index));
            ci.cancel();
        }
    }

    private static String format(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}
