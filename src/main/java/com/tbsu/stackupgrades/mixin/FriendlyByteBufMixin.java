/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin;

import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item counts travel as a single byte on the network, so a backpack slot holding
 * more than 127 items would reach the client as an empty or negative stack while
 * the server keeps the real amount. The menu then shows an empty slot and the
 * client and server disagree on it.
 *
 * <p>The stream layout stays exactly vanilla: the byte keeps carrying a clamped
 * count and the real amount rides along inside the item's NBT, which a client
 * without this mod simply ignores.</p>
 */
@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {
    @Unique
    private static final String COUNT_KEY = "tbstackupgrades:count";

    @Unique
    private static final ThreadLocal<CompoundTag> tbstackupgrades$readTag = new ThreadLocal<>();

    @Redirect(method = "writeItemStack(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/network/FriendlyByteBuf;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"))
    private int tbstackupgrades$clampCount(ItemStack stack) {
        return Math.min(stack.getCount(), 127);
    }

    @Redirect(method = "writeItemStack(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/network/FriendlyByteBuf;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getTag()Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag tbstackupgrades$tagWithCount(ItemStack stack) {
        return tbstackupgrades$withCount(stack, stack.getTag());
    }

    @Redirect(method = "readItem()Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;readNbt()Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag tbstackupgrades$captureTag(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        tbstackupgrades$readTag.set(tag);
        return tag;
    }

    @Inject(method = "readItem()Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void tbstackupgrades$restoreCount(CallbackInfoReturnable<ItemStack> cir) {
        CompoundTag tag = tbstackupgrades$readTag.get();
        tbstackupgrades$readTag.remove();
        if (tag == null || !tag.contains(COUNT_KEY)) {
            return;
        }

        ItemStack stack = cir.getReturnValue();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int count = tag.getInt(COUNT_KEY);
        if (count > stack.getCount()) {
            stack.setCount(Math.min(count, StackUpgradesConfig.HARD_MAX_SLOT_LIMIT));
            cir.setReturnValue(stack);
        }
    }

    /**
     * Copies the tag before adding the real count: the tag handed out by the item
     * belongs to the stack and must not be modified in place.
     */
    @Unique
    private static CompoundTag tbstackupgrades$withCount(ItemStack stack, CompoundTag tag) {
        int count = stack.getCount();
        if (count <= 127) {
            return tag;
        }
        CompoundTag copy = tag == null ? new CompoundTag() : tag.copy();
        copy.putInt(COUNT_KEY, count);
        return copy;
    }
}
