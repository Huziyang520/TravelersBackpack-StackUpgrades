/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.item;

import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tiviacz.travelersbackpack.item.upgrade.UpgradeItem;
import com.tbsu.stackupgrades.upgrade.StackTier;
import com.tbsu.stackupgrades.upgrade.StackUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;
import java.util.function.Consumer;

public class StackUpgradeItem extends UpgradeItem {
    private final StackTier tier;

    public StackUpgradeItem(Item.Properties properties, StackTier tier) {
        super(properties, null);
        this.tier = tier;
    }

    public StackTier getTier() {
        return this.tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> consumer, TooltipFlag flag) {
        // The two numbers are what decides whether the item is worth a slot, so
        // they go first and in the same blue the backpack uses for its own
        // upgrade lines; the description the base class adds follows below.
        consumer.accept(Component.translatable("item.tbstackupgrades.tooltip.multiplier", format(this.tier.multiplier()))
                .withStyle(ChatFormatting.BLUE));
        consumer.accept(Component.translatable("item.tbstackupgrades.tooltip.slot_limit", this.tier.capacityFor64())
                .withStyle(ChatFormatting.BLUE));
        consumer.accept(Component.translatable("item.tbstackupgrades.tooltip.stacking")
                .withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipDisplay, consumer, flag);
    }

    protected static String format(double multiplier) {
        if (multiplier == Math.floor(multiplier)) {
            return Integer.toString((int) multiplier);
        }
        return Double.toString(multiplier);
    }

    @Override
    public Class<? extends UpgradeBase<?>> getUpgradeClass() {
        return StackUpgrade.classFor(this.tier);
    }

    @Override
    public TriFunction<UpgradeManager, Integer, ItemStack, Optional<? extends UpgradeBase<?>>> getUpgrade() {
        return (manager, dataHolderSlot, provider) -> Optional.of(StackUpgrade.create(this.tier, manager, dataHolderSlot));
    }
}
