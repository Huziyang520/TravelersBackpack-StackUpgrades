/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin.client;

import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tbsu.stackupgrades.client.StackUpgradeWarning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

/**
 * Draws the "cannot remove upgrade" banner above the backpack UI (below it when
 * there is no room at the top).
 *
 * <p>The banner belongs to the backpack screen only. Drawing it from the vanilla
 * container screen made it show up over every open container for the length of
 * the warning, which looked like the warning fired while items were merely being
 * moved.</p>
 */
@Mixin(BackpackScreen.class)
public abstract class BackpackScreenWarningMixin {
    @Inject(method = "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At("TAIL"), remap = false)
    private void tbstackupgrades$renderWarning(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                               CallbackInfo ci) {
        if (!StackUpgradeWarning.active()) {
            return;
        }

        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        Font font = Minecraft.getInstance().font;
        Component message = Component.translatable("message.tbstackupgrades.cannot_remove_upgrade",
                String.format(Locale.ROOT, "%,d", StackUpgradeWarning.limit()));

        int boxWidth = font.width(message) + 12;
        int boxHeight = 14;

        int screenWidth = ((Screen) (Object) this).width;
        int screenHeight = ((Screen) (Object) this).height;
        int centerX = self.getGuiLeft() + self.getXSize() / 2;
        int x = Math.max(2, Math.min(centerX - boxWidth / 2, screenWidth - boxWidth - 2));

        int y = self.getGuiTop() - boxHeight - 5;
        if (y < 2) {
            int below = self.getGuiTop() + self.getYSize() + 5;
            // very small windows can leave no room at all - then pin the banner to
            // the top edge of the UI instead of drawing it off-screen
            y = below + boxHeight <= screenHeight - 2 ? below : Math.max(2, self.getGuiTop() + 3);
        }

        graphics.fill(x, y, x + boxWidth, y + boxHeight, 0xE0202020);
        graphics.fill(x, y, x + boxWidth, y + 1, 0xFFC84040);
        graphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0xFFC84040);
        graphics.fill(x, y, x + 1, y + boxHeight, 0xFFC84040);
        graphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, 0xFFC84040);

        graphics.drawString(font, message, x + 6, y + 3, 0xFFFF6060, true);
    }
}
