/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin.client;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.menu.slot.UpgradeLockableSlotItemHandler;
import com.tbsu.stackupgrades.client.StackUpgradeWarning;
import com.tbsu.stackupgrades.util.SlotLimitProvider;
import com.tbsu.stackupgrades.util.StackLimitProvider;
import com.tbsu.stackupgrades.util.StackLimits;
import com.tbsu.stackupgrades.util.UpgradeRemovalGuard;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Client side additions for container screens:
 * <ul>
 *     <li>adds a "amount / capacity" line to the tooltip of backpack slots, mirroring
 *     Sophisticated Backpacks;</li>
 *     <li>refuses clicks on an upgrade slot while oversized stacks would be left
 *     behind, and draws the warning as a red banner (the action bar is not
 *     rendered while a container screen is open).</li>
 * </ul>
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    protected Slot hoveredSlot;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected int imageHeight;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
    private void tbstackupgrades$addCapacityLine(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        Slot slot = this.hoveredSlot;
        if (slot == null || stack.isEmpty() || !(slot instanceof SlotLimitProvider slotProvider)) {
            return;
        }

        StackLimitProvider provider = slotProvider.tbsu$limitProvider();
        if (provider == null) {
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

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$guardUpgradeSlotClick(Slot slot, int slotId, int buttonNum, ClickType clickType,
                                                       CallbackInfo ci) {
        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE) {
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

    /**
     * Draws the "cannot remove upgrade" warning as a banner right above the UI
     * (below it when there is no room at the top). A fixed banner is where
     * Sophisticated Backpacks shows its warning as well; a cursor tooltip was easy
     * to miss and got clipped by the GUI.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void tbstackupgrades$renderWarning(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                               CallbackInfo ci) {
        if (!StackUpgradeWarning.active()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        Component message = Component.translatable("message.tbstackupgrades.cannot_remove_upgrade",
                String.format(Locale.ROOT, "%,d", StackUpgradeWarning.limit()));

        int textWidth = font.width(message);
        int boxWidth = textWidth + 12;
        int boxHeight = 14;

        int centerX = this.leftPos + this.imageWidth / 2;
        int x = centerX - boxWidth / 2;
        int screenWidth = ((Screen) (Object) this).width;
        x = Math.max(2, Math.min(x, screenWidth - boxWidth - 2));

        int screenHeight = ((Screen) (Object) this).height;
        int y = this.topPos - boxHeight - 5;
        if (y < 2) {
            int below = this.topPos + this.imageHeight + 5;
            // very small windows can leave no room at all - then pin the banner to
            // the top edge of the UI instead of drawing it off-screen
            y = below + boxHeight <= screenHeight - 2 ? below : Math.max(2, this.topPos + 3);
        }

        graphics.fill(x, y, x + boxWidth, y + boxHeight, 0xE0202020);
        graphics.fill(x, y, x + boxWidth, y + 1, 0xFFC84040);
        graphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0xFFC84040);
        graphics.fill(x, y, x + 1, y + boxHeight, 0xFFC84040);
        graphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, 0xFFC84040);

        graphics.drawString(font, message, x + 6, y + 3, 0xFFFF6060, true);
    }

    @Unique
    private static String format(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}
