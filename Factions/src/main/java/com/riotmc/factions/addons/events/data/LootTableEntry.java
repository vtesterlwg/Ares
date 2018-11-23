package com.riotmc.factions.addons.events.data;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public final class LootTableEntry {
    @Getter public final ItemStack item;
    @Getter public final boolean lucky;
    @Getter public final float chance;

    public LootTableEntry(ItemStack item, float chance) {
        this.item = item;
        this.lucky = false;
        this.chance = chance;
    }

    public LootTableEntry(ItemStack item, boolean lucky, float chance) {
        this.item = item;
        this.lucky = lucky;
        this.chance = chance;
    }
}
