package com.playares.factions.addons.staff.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.item.custom.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class NavigatorItem implements CustomItem {
    @Override
    public Material getMaterial() {
        return Material.COMPASS;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + "Navigate";
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