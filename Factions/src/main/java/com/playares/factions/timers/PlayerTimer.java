package com.playares.factions.timers;

import com.playares.commons.bukkit.timer.Timer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.UUID;

public abstract class PlayerTimer extends Timer {
    @Getter
    public final UUID owner;

    @Getter
    public final PlayerTimerType type;

    public PlayerTimer(UUID owner, PlayerTimerType type, int seconds) {
        super(seconds);
        this.owner = owner;
        this.type = type;
    }

    @AllArgsConstructor
    public enum PlayerTimerType {
        ENDERPEARL(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Enderpearl", true, true),
        HOME(ChatColor.BLUE + "" + ChatColor.BOLD + "Home", true, true),
        STUCK(ChatColor.BLUE + "" + ChatColor.BOLD + "Stuck", true, false),
        CRAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Crapple", false, false),
        GAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Gapple", false, false),
        TOTEM(ChatColor.GOLD + "" + ChatColor.BOLD + "Totem", false, false),
        LOGOUT(ChatColor.AQUA + "" + ChatColor.BOLD + "Logout", true, true),
        COMBAT(ChatColor.RED + "" + ChatColor.BOLD + "Combat Tag", true, true),
        PROTECTION(ChatColor.GREEN + "" + ChatColor.BOLD + "Protection", true, false);

        @Getter
        public final String displayName;

        @Getter
        public final boolean render;

        @Getter
        public final boolean decimal;
    }
}
