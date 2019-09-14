package com.playares.arena.timer;

import com.playares.commons.bukkit.timer.Timer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.UUID;

public abstract class PlayerTimer extends Timer {
    @Getter public final UUID owner;
    @Getter public final PlayerTimerType type;

    public PlayerTimer(UUID owner, PlayerTimerType type, int seconds) {
        super(seconds);
        this.owner = owner;
        this.type = type;
    }

    @AllArgsConstructor
    public enum PlayerTimerType {
        ENDERPEARL(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Enderpearl", true, true),
        CRAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Crapple", true, true),
        CLASS(ChatColor.BLUE + "" + ChatColor.BOLD + "Class", true, true),
        MATCH_STARTING(ChatColor.GREEN + "" + ChatColor.BOLD + "Match Starting", true, true),
        MATCH_ENDING(ChatColor.RED + "" + ChatColor.BOLD + "Match Ending", true, true);

        @Getter public final String displayName;
        @Getter public final boolean render;
        @Getter public final boolean decimal;
    }
}