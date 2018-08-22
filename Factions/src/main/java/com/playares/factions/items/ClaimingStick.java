package com.playares.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.factions.Factions;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class ClaimingStick implements CustomItem {
    @Getter
    public final Factions plugin;

    public ClaimingStick(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.STICK;
    }

    @Override
    public String getName() {
        return ChatColor.GREEN + "Claiming Stick";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.YELLOW + "Left-click to set " + ChatColor.BLUE + "Corner A");
        lore.add(ChatColor.YELLOW + "Right-click to set " + ChatColor.BLUE + "Corner B");
        lore.add(ChatColor.YELLOW + "Left-click " + ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "while sneaking" + ChatColor.YELLOW + " to confirm claim");
        lore.add(ChatColor.YELLOW + "Right-click the air to " + ChatColor.RED + "cancel" + ChatColor.YELLOW + " the claim");

        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public boolean isSoulbound() {
        return true;
    }
}
