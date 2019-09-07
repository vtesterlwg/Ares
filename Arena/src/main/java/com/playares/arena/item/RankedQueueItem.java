package com.playares.arena.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.arena.Arenas;
import com.playares.commons.bukkit.item.custom.CustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class RankedQueueItem implements CustomItem {
    @Getter public final Arenas plugin;

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_SWORD;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Ranked Queue";
    }

    @Override
    public List<String> getLore() {
        return Lists.newArrayList();
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {

        };
    }
}
