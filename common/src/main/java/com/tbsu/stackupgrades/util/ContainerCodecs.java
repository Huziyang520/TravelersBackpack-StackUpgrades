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
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container codecs that tolerate oversized stacks.
 * <p>
 * Vanilla limits the {@code count} of an item stack to 1..99 during
 * serialization ({@code ItemStackTemplate.MAP_CODEC} / {@code ItemStack.MAP_CODEC}).
 * Traveler's Backpack persists its contents through {@code ItemContainerContents},
 * so slots holding more than 99 items would fail to save. These codecs encode the
 * same layout without the artificial cap, which is required for the higher stack
 * upgrade tiers (128 / 256 / 512 / 1024).
 */
public final class ContainerCodecs {
    private static final Codec<ItemStackTemplate> TEMPLATE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Item.CODEC.fieldOf("id").forGetter(ItemStackTemplate::item),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ItemStackTemplate::count),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStackTemplate::components)
    ).apply(instance, ItemStackTemplate::new));

    private static final Codec<SlotEntry> SLOT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 255).fieldOf("slot").forGetter(SlotEntry::index),
            TEMPLATE_CODEC.fieldOf("item").forGetter(SlotEntry::template)
    ).apply(instance, SlotEntry::new));

    public static final Codec<ItemContainerContents> CONTAINER_CODEC = SLOT_CODEC
            .sizeLimitedListOf(256)
            .xmap(ContainerCodecs::toContents, ContainerCodecs::fromContents);

    private ContainerCodecs() {
    }

    private static ItemContainerContents toContents(List<SlotEntry> entries) {
        int maxIndex = -1;
        for (SlotEntry entry : entries) {
            maxIndex = Math.max(maxIndex, entry.index());
        }
        if (maxIndex < 0) {
            return ItemContainerContents.EMPTY;
        }

        List<ItemStack> stacks = new ArrayList<>(Collections.nCopies(maxIndex + 1, ItemStack.EMPTY));
        for (SlotEntry entry : entries) {
            ItemStackTemplate template = entry.template();
            stacks.set(entry.index(), createStack(template));
        }
        return ItemContainerContents.fromItems(stacks);
    }

    private static List<SlotEntry> fromContents(ItemContainerContents contents) {
        int size = contents.size();
        if (size <= 0) {
            return List.of();
        }

        NonNullList<ItemStack> stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        contents.copyInto(stacks);

        List<SlotEntry> entries = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                entries.add(new SlotEntry(i, ItemStackTemplate.fromNonEmptyStack(stack)));
            }
        }
        return entries;
    }

    /**
     * Builds the stack directly, bypassing {@code ItemStackTemplate.create()}
     * which would reject counts above the vanilla stack size.
     */
    public static ItemStack createStack(ItemStackTemplate template) {
        return new ItemStack(template.typeHolder(), template.count(), template.components());
    }

    private record SlotEntry(int index, ItemStackTemplate template) {
    }
}
