/*
 * This file is part of Stack Upgrades, an addon for Traveler's Backpack.
 * SPDX-License-Identifier: MIT
 *
 * 本文件为原创代码，以 MIT 协议发布（见 LICENSE）。
 * 注意：mixin/ 目录下的注入层类采用 LGPL-3.0-or-later OR MIT 双许可。
 */

package com.tbsu.stackupgrades.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tiviacz.travelersbackpack.components.BackpackContainerContents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container codec that tolerates oversized stacks.
 * <p>
 * Vanilla caps the {@code count} of a serialized item stack at 1..99
 * ({@code ItemStack.CODEC}), and Traveler's Backpack persists its contents
 * through its own container component. Slots holding more than 99 items would
 * therefore fail to save. This codec writes the same layout (a {@code slot} /
 * {@code item} list) with a cap of {@link StackUpgradesConfig#HARD_MAX_SLOT_LIMIT}
 * instead, and builds stacks directly so loading never runs the vanilla
 * stack-size validation.
 */
public final class ContainerCodecs {
    private static final Codec<ItemStack> ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
            Codec.intRange(1, StackUpgradesConfig.HARD_MAX_SLOT_LIMIT)
                    .optionalFieldOf("count", 1)
                    .forGetter(ItemStack::getCount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                    .forGetter(ItemStack::getComponentsPatch)
    ).apply(instance, ItemStack::new));

    private static final Codec<SlotEntry> SLOT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 255).fieldOf("slot").forGetter(SlotEntry::index),
            ITEM_CODEC.fieldOf("item").forGetter(SlotEntry::item)
    ).apply(instance, SlotEntry::new));

    public static final Codec<BackpackContainerContents> CONTAINER_CODEC = SLOT_CODEC
            .sizeLimitedListOf(256)
            .xmap(ContainerCodecs::toContents, ContainerCodecs::fromContents);

    private ContainerCodecs() {
    }

    private static BackpackContainerContents toContents(List<SlotEntry> entries) {
        int maxIndex = -1;
        for (SlotEntry entry : entries) {
            maxIndex = Math.max(maxIndex, entry.index());
        }
        if (maxIndex < 0) {
            return BackpackContainerContents.EMPTY;
        }

        List<ItemStack> stacks = new ArrayList<>(Collections.nCopies(maxIndex + 1, ItemStack.EMPTY));
        for (SlotEntry entry : entries) {
            stacks.set(entry.index(), entry.item());
        }
        return BackpackContainerContents.fromItems(stacks.size(), stacks);
    }

    private static List<SlotEntry> fromContents(BackpackContainerContents contents) {
        NonNullList<ItemStack> stacks = contents.getItems();
        if (stacks.isEmpty()) {
            return List.of();
        }

        List<SlotEntry> entries = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                entries.add(new SlotEntry(i, stack));
            }
        }
        return entries;
    }

    private record SlotEntry(int index, ItemStack item) {
    }
}
