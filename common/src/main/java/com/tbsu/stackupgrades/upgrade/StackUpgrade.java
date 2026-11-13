/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.upgrade;

import com.mojang.datafixers.util.Pair;
import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.widgets.WidgetBase;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.menu.BackpackBaseMenu;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * The upgrade itself: a passive upgrade without its own tab. The multiplier is
 * taken into account by {@code StackLimits} whenever the backpack storage
 * handler is asked about a slot limit.
 */
public class StackUpgrade extends UpgradeBase<StackUpgrade> {
    private final StackTier tier;

    public StackUpgrade(UpgradeManager manager, int dataHolderSlot, StackTier tier) {
        super(manager, dataHolderSlot, new Point(24, 24));
        this.tier = tier;
    }

    public StackTier getTier() {
        return this.tier;
    }

    public double getMultiplier() {
        return this.tier.multiplier();
    }

    @Override
    public boolean hasTab() {
        return false;
    }

    /**
     * Sidebar icon. Only Fabric's client screen interface declares this method,
     * so it is not annotated as an override: on NeoForge it is simply never
     * called and the upgrade works without its own icon.
     */
    public WidgetBase<?> createWidget(BackpackScreen screen, int x, int y) {
        return new StackUpgradeWidget(screen, this, new Point(screen.getGuiLeft() + x, screen.getGuiTop() + y));
    }

    @Override
    public List<Pair<Integer, Integer>> getUpgradeSlotsPosition(int x, int y) {
        return List.of();
    }

    @Override
    public List<? extends Slot> getUpgradeSlots(BackpackBaseMenu menu, BackpackWrapper wrapper, int x, int y) {
        return List.of();
    }

    public static StackUpgrade create(StackTier tier, UpgradeManager manager, int dataHolderSlot) {
        return switch (tier) {
            case STARTER -> new Starter(manager, dataHolderSlot);
            case TIER_1 -> new Tier1(manager, dataHolderSlot);
            case TIER_2 -> new Tier2(manager, dataHolderSlot);
            case TIER_3 -> new Tier3(manager, dataHolderSlot);
            case TIER_4 -> new Tier4(manager, dataHolderSlot);
        };
    }

    public static Class<? extends StackUpgrade> classFor(StackTier tier) {
        return switch (tier) {
            case STARTER -> Starter.class;
            case TIER_1 -> Tier1.class;
            case TIER_2 -> Tier2.class;
            case TIER_3 -> Tier3.class;
            case TIER_4 -> Tier4.class;
        };
    }

    /**
     * Traveler's Backpack only allows one upgrade of the same class per backpack,
     * so every tier gets its own class. Different tiers can therefore be combined,
     * exactly like Sophisticated Backpacks multiplies its stack upgrades.
     */
    public static class Starter extends StackUpgrade {
        public Starter(UpgradeManager manager, int dataHolderSlot) {
            super(manager, dataHolderSlot, StackTier.STARTER);
        }
    }

    public static class Tier1 extends StackUpgrade {
        public Tier1(UpgradeManager manager, int dataHolderSlot) {
            super(manager, dataHolderSlot, StackTier.TIER_1);
        }
    }

    public static class Tier2 extends StackUpgrade {
        public Tier2(UpgradeManager manager, int dataHolderSlot) {
            super(manager, dataHolderSlot, StackTier.TIER_2);
        }
    }

    public static class Tier3 extends StackUpgrade {
        public Tier3(UpgradeManager manager, int dataHolderSlot) {
            super(manager, dataHolderSlot, StackTier.TIER_3);
        }
    }

    public static class Tier4 extends StackUpgrade {
        public Tier4(UpgradeManager manager, int dataHolderSlot) {
            super(manager, dataHolderSlot, StackTier.TIER_4);
        }
    }
}
