package com.playares.arena.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.arena.Arenas;
import com.playares.arena.menu.TeamMenu;
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
public final class OtherTeamItem implements CustomItem {
    @Getter public final Arenas plugin;

    @Override
    public Material getMaterial() {
        return Material.SKULL_ITEM;
    }

    @Override
    public short getDurability() {
        return 3;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + "View Other Teams";
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
            final TeamMenu menu = new TeamMenu(plugin, who);
            menu.open();
        };
    }
}
