package com.playares.factions.addons.staff.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.item.custom.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class InvseeItem implements CustomItem {
    @Override
    public Material getMaterial() {
        return Material.BOOK;
    }

    @Override
    public String getName() {
        return ChatColor.GREEN + "View Player Inventory";
    }

    @Override
    public List<String> getLore() {
        return Lists.newArrayList();
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }
}
