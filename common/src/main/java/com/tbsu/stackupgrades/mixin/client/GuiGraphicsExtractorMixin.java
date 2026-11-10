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

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla draws item counts right aligned without checking whether they fit, so
 * a slot holding 128 / 256 / 1024 items would paint the number over the
 * neighbouring slots. This scales such numbers down until they fit into the
 * 16x16 icon, which keeps the backpack readable with high stack upgrades.
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    @Inject(method = "itemCount", at = @At("HEAD"), cancellable = true)
    private void tbstackupgrades$fitItemCount(Font font, ItemStack itemStack, int x, int y, String countText, CallbackInfo ci) {
        if (itemStack.getCount() == 1 && countText == null) {
            return;
        }

        String amount = countText == null ? String.valueOf(itemStack.getCount()) : countText;
        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;

        int width = font.width(amount);
        int right = x + 19 - 2;
        int baseline = y + 6 + 3;

        if (width <= 16) {
            graphics.text(font, amount, right - width, baseline, -1, true);
        } else {
            float scale = 16.0F / (float) width;
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate((float) right, (float) baseline);
            pose.scale(scale, scale);
            graphics.text(font, amount, -width, 0, -1, true);
            pose.popMatrix();
        }

        ci.cancel();
    }
}
