package com.riotmc.factions.timers;

import com.riotmc.commons.bukkit.timer.Timer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.UUID;

/**
 * Represents a timer for Players
 */
public abstract class PlayerTimer extends Timer {
    /** The owner of this timer **/
    @Getter public final UUID owner;
    /** The type of this timer **/
    @Getter public final PlayerTimerType type;

    public PlayerTimer(UUID owner, PlayerTimerType type, int seconds) {
        super(seconds);
        this.owner = owner;
        this.type = type;
    }

    public PlayerTimer(UUID owner, PlayerTimerType type, long milliseconds) {
        super(milliseconds);
        this.owner = owner;
        this.type = type;
    }

    @AllArgsConstructor
    public enum PlayerTimerType {
        ENDERPEARL(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Enderpearl", true, true),
        HOME(ChatColor.BLUE + "" + ChatColor.BOLD + "Home", true, true),
        STUCK(ChatColor.BLUE + "" + ChatColor.BOLD + "Stuck", true, false),
        CRAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Crapple", true, true),
        GAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Gapple", false, false),
        TOTEM(ChatColor.GOLD + "" + ChatColor.BOLD + "Totem", false, false),
        LOGOUT(ChatColor.AQUA + "" + ChatColor.BOLD + "Logout", true, true),
        COMBAT(ChatColor.RED + "" + ChatColor.BOLD + "Combat Tag", true, true),
        PROTECTION(ChatColor.GREEN + "" + ChatColor.BOLD + "Protection", true, false);

        /** The display name for this timer, usually used in HUD rendering **/
        @Getter public final String displayName;
        /** If true, this timer type will be rendering in the HUD **/
        @Getter public final boolean render;
        /** If true, the remaining time on this timer will be displayed in decimal format **/
        @Getter public final boolean decimal;

        /**
         * Returns a PlayerTimerType matching the provided name
         * @param name Name
         * @return PlayerTimerType
         */
        public static PlayerTimerType match(String name) {
            final PlayerTimerType type;

            try {
                type = PlayerTimerType.valueOf(name);
            } catch (IllegalArgumentException ex) {
                return null;
            }

            return type;
        }
    }
}
