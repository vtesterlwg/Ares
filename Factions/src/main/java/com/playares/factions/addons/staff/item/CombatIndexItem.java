package com.playares.factions.addons.staff.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.factions.Factions;
import com.playares.factions.addons.staff.menu.CombatIndexMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class CombatIndexItem implements CustomItem {
    @Getter public final Factions plugin;

    @Override
    public Material getMaterial() {
        return Material.GOLD_SWORD;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "View Combat Tagged Players";
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
            final CombatIndexMenu menu = new CombatIndexMenu(plugin, who);
            menu.open();
        };
    }
}