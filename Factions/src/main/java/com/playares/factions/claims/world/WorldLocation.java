package com.playares.factions.claims.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum WorldLocation {
    OVERWORLD_WARZONE(ChatColor.RED + "WarZone"),
    OVERWORLD_WILDERNESS(ChatColor.DARK_GREEN + "Wilderness"),
    NETHER_WARZONE(ChatColor.DARK_RED + "Nether WarZone"),
    NETHER_WILDERNESS(ChatColor.DARK_RED + "Nether Wilderness"),
    THE_END(ChatColor.DARK_PURPLE + "The End"),
    THE_END_CITY(ChatColor.LIGHT_PURPLE + "The End City"),
    UNKNOWN(ChatColor.GRAY + "? ? ?");

    @Getter public final String displayName;
}
