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

        lore.add(ChatColor.GOLD + "Left-click" + ChatColor.YELLOW + " to set " + ChatColor.BLUE + "Corner A");
        lore.add(ChatColor.GOLD + "Right-click" + ChatColor.YELLOW + " to set " + ChatColor.BLUE + "Corner B");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.AQUA + "With both corners set,");
        lore.add(ChatColor.AQUA + "Left-click " + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "while sneaking" + ChatColor.AQUA + " to confirm");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.RED + "To cancel the claiming process,");
        lore.add(ChatColor.RED + "Right-click " + ChatColor.RED + "" + ChatColor.UNDERLINE + "while sneaking" + ChatColor.RESET);
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.DARK_PURPLE + "Tips" + ChatColor.LIGHT_PURPLE + ": ");
        lore.add(ChatColor.YELLOW + " - " + ChatColor.GOLD + "All claims must be connected");
        lore.add(ChatColor.YELLOW + " - " + ChatColor.GOLD + "Claims must be " + plugin.getFactionConfig().getPlayerClaimBuffer() + " blocks away from other faction claims");
        lore.add(ChatColor.YELLOW + " - " + ChatColor.GOLD + "Claims can not be near Server Claims");
        lore.add(ChatColor.YELLOW + " - " + ChatColor.GOLD + "Claims can only be created in the Overworld");

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
