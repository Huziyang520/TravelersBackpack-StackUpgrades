/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 *
 * License: SPDX-License-Identifier: LGPL-3.0-or-later OR MIT
 *
 * This class is part of the Mixin layer and injects into Traveler's Backpack
 * (LGPL-3.0) and/or Minecraft classes at runtime.
 */

package com.tbsu.stackupgrades.mixin;

import com.tbsu.stackupgrades.util.ClickLimits;
import com.tbsu.stackupgrades.util.StackUpgradesConfig;
import com.tbsu.stackupgrades.util.StackLimits;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Shift-click transfer only merges stacks when {@code ItemStack#isStackable()} is
 * true, which is decided by the item's own max stack size. Items whose max size
 * is 1 therefore never merge into an upgraded backpack slot. While the move
 * targets backpack slots with a multiplier, they are treated as stackable.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Unique
    private static final ThreadLocal<Boolean> tbstackupgrades$stackable = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(method = "moveItemStackTo", at = @At("HEAD"))
    private void tbstackupgrades$markRange(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection,
                                           CallbackInfoReturnable<Boolean> cir) {
        tbstackupgrades$stackable.set(StackUpgradesConfig.quickTransferStacking
                && !stack.isEmpty()
                && StackLimits.hasUpgradedSlot((AbstractContainerMenu) (Object) this, startIndex, endIndex));
    }

    @Inject(method = "moveItemStackTo", at = @At("RETURN"))
    private void tbstackupgrades$clearRange(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection,
                                            CallbackInfoReturnable<Boolean> cir) {
        tbstackupgrades$stackable.remove();
    }

    @Redirect(method = "moveItemStackTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isStackable()Z"))
    private boolean tbstackupgrades$treatAsStackable(ItemStack stack) {
        return stack.isStackable() || tbstackupgrades$stackable.get();
    }

    /**
     * Right-click picks up half of the stack; on an oversized stack that is far
     * more than the player wants, so the taken amount is clamped.
     */
    @Inject(method = "doClick", at = @At("HEAD"))
    private void tbstackupgrades$rememberButton(int slotId, int button, ContainerInput input, Player player,
                                                CallbackInfo ci) {
        ClickLimits.setButton(button);
    }

    @Inject(method = "doClick", at = @At("RETURN"))
    private void tbstackupgrades$forgetButton(int slotId, int button, ContainerInput input, Player player,
                                              CallbackInfo ci) {
        ClickLimits.clearButton();
    }

    @Redirect(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;tryRemove(IILnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;"))
    private Optional<ItemStack> tbstackupgrades$clampPickup(Slot slot, int amount, int maxAmount, Player player) {
        return slot.tryRemove(ClickLimits.clampPickup(amount, slot.getItem().getCount()), maxAmount, player);
    }
}
