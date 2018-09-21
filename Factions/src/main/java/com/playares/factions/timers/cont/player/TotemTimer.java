package com.playares.factions.timers.cont.player;

import com.playares.factions.timers.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a players Totem of the Undying timer
 */
public final class TotemTimer extends PlayerTimer {
    public TotemTimer(UUID owner, int seconds) {
        super(owner, PlayerTimerType.TOTEM, seconds);
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your totems have been unlocked");
    }
}
