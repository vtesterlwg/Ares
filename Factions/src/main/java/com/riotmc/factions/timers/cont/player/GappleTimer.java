package com.riotmc.factions.timers.cont.player;

import com.riotmc.factions.timers.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a players gapple timer
 */
public final class GappleTimer extends PlayerTimer {
    public GappleTimer(UUID owner, int seconds) {
        super(owner, PlayerTimerType.GAPPLE, seconds);
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your gapples have been unlocked");
    }
}