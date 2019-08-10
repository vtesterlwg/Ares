package com.playares.factions.addons.crowbars;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.item.custom.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class CrowbarItem implements CustomItem {
    @Override
    public Material getMaterial() {
        return Material.DIAMOND_HOE;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_RED + "Crowbar";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.GOLD + "Right-click" + ChatColor.YELLOW + " a " + ChatColor.DARK_GREEN + "Monster Spawner" + ChatColor.YELLOW + " to obtain it.");
        lore.add(ChatColor.YELLOW + "This item can only be used " + ChatColor.RED + "one" + ChatColor.YELLOW + " time.");

        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }
}
