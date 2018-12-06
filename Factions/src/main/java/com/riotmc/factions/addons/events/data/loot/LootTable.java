package com.riotmc.factions.addons.events.data.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public final class LootTable {
    @Getter public final String name;
    @Getter public final Set<LootTableEntry> entries;

    public LootTable(String name) {
        this.name = name;
        this.entries = Sets.newConcurrentHashSet();
    }

    public LootTable(String name, Collection<LootTableEntry> entries) {
        this.name = name;
        this.entries = Sets.newConcurrentHashSet(entries);
    }

    public ImmutableList<ItemStack> getLoot(Player player, int size) {
        final List<ItemStack> result = Lists.newArrayList();
        final boolean lucky = player.hasPotionEffect(PotionEffectType.LUCK);
        final Random random = new Random();
        int attempts = 0;

        for (LootTableEntry entry : entries.stream().filter(e -> e.lucky == lucky).collect(Collectors.toList())) {
            if (result.size() >= size || attempts >= 30) {
                break;
            }

            final float pull = random.nextFloat();

            if (pull <= entry.getChance()) {
                result.add(entry.getItem());
            }

            attempts++;
        }

        return ImmutableList.copyOf(result);
    }
}