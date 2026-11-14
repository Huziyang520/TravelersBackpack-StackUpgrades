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
import net.minecraft.world.entity.player.Inventory;
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
 * <p>
 * The click state is tracked on {@code clicked} rather than {@code doClick}:
 * Traveler's Backpack overrides {@code doClick} and does not always delegate to
 * it, while {@code clicked} is always the entry point.
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
        ClickLimits.beginMove(stack);
    }

    @Inject(method = "moveItemStackTo", at = @At("RETURN"))
    private void tbstackupgrades$clearRange(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection,
                                            CallbackInfoReturnable<Boolean> cir) {
        tbstackupgrades$stackable.remove();
        ClickLimits.endMove();
    }

    @Redirect(method = "moveItemStackTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isStackable()Z"))
    private boolean tbstackupgrades$treatAsStackable(ItemStack stack) {
        return stack.isStackable() || tbstackupgrades$stackable.get();
    }

    /** Caps how much one shift click may move in total, see {@link ClickLimits#allowance}. */
    @Redirect(method = "moveItemStackTo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I"))
    private int tbstackupgrades$capTransfer(Slot slot, ItemStack stack) {
        return ClickLimits.allowance(slot.getMaxStackSize(stack), stack);
    }

    @Inject(method = "clicked", at = @At("HEAD"))
    private void tbstackupgrades$beginClick(int slotId, int button, ContainerInput input, Player player,
                                            CallbackInfo ci) {
        int budget = 0;
        if (input == ContainerInput.QUICK_MOVE && StackUpgradesConfig.shiftTransferLimit
                && slotId >= 0 && slotId < this.slots.size()) {
            ItemStack stack = this.slots.get(slotId).getItem();
            int max = stack.getMaxStackSize();
            if (!stack.isEmpty() && stack.getCount() > max) {
                budget = max;
            }
        }
        ClickLimits.beginClick(button, budget);
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void tbstackupgrades$endClick(int slotId, int button, ContainerInput input, Player player,
                                          CallbackInfo ci) {
        ClickLimits.endClick();
    }

    /** Pickup / quick-move of an oversized stack never hands out more than one stack. */
    @Redirect(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;tryRemove(IILnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;"))
    private Optional<ItemStack> tbstackupgrades$clampPickup(Slot slot, int amount, int maxAmount, Player player) {
        return slot.tryRemove(ClickLimits.clampPickup(amount, slot.getItem()), maxAmount, player);
    }

    /**
     * Throwing and pick-all take items out of an oversized slot. Both would
     * otherwise drop a whole 1024 stack as a single entity, which vanilla cannot
     * serialize (counts above 99), so the items would be gone after a reload.
     */
    @Redirect(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;safeTake(IILnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack tbstackupgrades$clampSafeTake(Slot slot, int amount, int maxAmount, Player player) {
        return slot.safeTake(ClickLimits.clampExtract(amount, slot.getItem()), maxAmount, player);
    }

    /**
     * Number-key swapping moves the slot contents into the player inventory, which
     * has no oversized-stack support. Split such a stack into regular stacks first
     * so nothing is lost when the world is saved.
     */
    @Redirect(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void tbstackupgrades$clampInventoryWrite(Inventory inventory, int slot, ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() <= stack.getMaxStackSize()) {
            inventory.setItem(slot, stack);
            return;
        }

        ItemStack remainder = stack.copy();
        int itemMax = Math.max(1, remainder.getMaxStackSize());
        inventory.setItem(slot, remainder.split(Math.min(itemMax, remainder.getCount())));
        if (!remainder.isEmpty()) {
            inventory.placeItemBackInInventory(remainder, net.minecraft.util.Prediction.SERVER_ONLY);
        }
    }

    /**
     * A cursor can still end up holding an oversized stack (for example by swapping
     * with a full slot). Vanilla hands the cursor back on close, and a dropped
     * entity is limited to 99 items, so an oversized one would be deleted on save.
     * Hand such a stack back in normal-sized pieces first.
     */
    @Inject(method = "removed", at = @At("HEAD"))
    private void tbstackupgrades$splitOversizedCarried(Player player, CallbackInfo ci) {
        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
        ItemStack carried = self.getCarried();
        if (carried.isEmpty() || carried.getCount() <= carried.getMaxStackSize()) {
            return;
        }

        // Only the server decides where the items go; the client menu is closed by
        // the server's packet right after.
        if (player.level().isClientSide()) {
            self.setCarried(ItemStack.EMPTY);
            return;
        }

        ItemStack remainder = carried.copy();
        self.setCarried(ItemStack.EMPTY);
        int itemMax = Math.max(1, remainder.getMaxStackSize());
        while (!remainder.isEmpty()) {
            player.getInventory().placeItemBackInInventory(
                    remainder.split(Math.min(itemMax, remainder.getCount())),
                    net.minecraft.util.Prediction.SERVER_ONLY);
        }
    }
}
