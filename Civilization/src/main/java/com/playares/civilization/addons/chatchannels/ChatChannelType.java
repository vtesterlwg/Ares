package com.playares.civilization.addons.chatchannels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum ChatChannelType {
    STAFF(ChatColor.DARK_RED + "Staff"),
    LOCAL(ChatColor.GREEN + "Local"),
    NETWORK(ChatColor.BLUE + "Network");

    @Getter public final String displayName;
}
