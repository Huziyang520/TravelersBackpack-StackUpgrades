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

import com.tiviacz.travelersbackpack.client.screens.AbstractBackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tbsu.stackupgrades.client.StackUpgradeWarning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

/**
 * Draws the "cannot remove upgrade" warning as a banner right above the backpack
 * UI (below it when there is no room at the top). It used to follow the cursor
 * as a tooltip, which was easy to miss and got clipped by the GUI; a fixed
 * banner is where Sophisticated Backpacks shows its warning as well.
 * <p>
 * The action bar is not rendered while a container screen is open, so the
 * banner has to be drawn by the screen itself.
 */
@Mixin(BackpackScreen.class)
public abstract class BackpackScreenMixin {
    private static final int BACKGROUND_COLOR = 0xE0202020;
    private static final int BORDER_COLOR = 0xFFC84040;
    private static final int TEXT_COLOR = 0xFFFF6060;

    @Inject(method = "extractTooltip", at = @At("TAIL"))
    private void tbstackupgrades$renderWarning(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (!StackUpgradeWarning.active()) {
            return;
        }

        AbstractBackpackScreen screen = (AbstractBackpackScreen) (Object) this;
        Font font = Minecraft.getInstance().font;
        Component message = Component.translatable("message.tbstackupgrades.cannot_remove_upgrade",
                String.format(Locale.ROOT, "%,d", StackUpgradeWarning.limit()));

        int textWidth = font.width(message);
        int boxWidth = textWidth + 12;
        int boxHeight = 14;

        int centerX = screen.getGuiLeft() + screen.getImageWidth() / 2;
        int x = centerX - boxWidth / 2;
        int screenWidth = ((Screen) (Object) this).width;
        x = Math.max(2, Math.min(x, screenWidth - boxWidth - 2));

        int screenHeight = ((Screen) (Object) this).height;
        int y = screen.getGuiTop() - boxHeight - 5;
        if (y < 2) {
            int below = screen.getGuiTop() + screen.getImageHeight() + 5;
            // very small windows can leave no room at all - then pin the banner to
            // the top edge of the UI instead of drawing it off-screen
            y = below + boxHeight <= screenHeight - 2 ? below : Math.max(2, screen.getGuiTop() + 3);
        }

        graphics.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND_COLOR);
        graphics.fill(x, y, x + boxWidth, y + 1, BORDER_COLOR);
        graphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, BORDER_COLOR);
        graphics.fill(x, y, x + 1, y + boxHeight, BORDER_COLOR);
        graphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, BORDER_COLOR);

        graphics.text(font, message, x + 6, y + 3, TEXT_COLOR, true);
    }
}
