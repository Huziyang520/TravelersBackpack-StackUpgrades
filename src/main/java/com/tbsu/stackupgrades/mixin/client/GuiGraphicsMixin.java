/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla draws item counts right aligned without checking whether they fit, so
 * a slot holding 128 / 256 / 1024 items would paint the number over the
 * neighbouring slots. This scales such numbers down until they fit into the
 * 16x16 icon, which keeps the backpack readable with high stack upgrades.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Redirect(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    private int tbstackupgrades$fitItemCount(GuiGraphics graphics, Font font, String text, int x, int y,
                                             int color, boolean shadow) {
        int width = font.width(text);
        if (width <= 16) {
            return graphics.drawString(font, text, x, y, color, shadow);
        }

        float scale = 16.0F / (float) width;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        // right edge of the count stays where vanilla puts it: shift the origin to
        // it, then shrink the text back towards it
        pose.translate((float) (x + width), (float) y, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.drawString(font, text, -width, 0, color, shadow);
        pose.popPose();
        return 0;
    }
}
