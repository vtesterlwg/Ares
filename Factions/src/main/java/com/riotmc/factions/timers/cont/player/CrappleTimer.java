package com.riotmc.factions.timers.cont.player;

import com.riotmc.factions.timers.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a players crapple timer
 */
public final class CrappleTimer extends PlayerTimer {
    public CrappleTimer(UUID owner, int seconds) {
        super(owner, PlayerTimerType.CRAPPLE, seconds);
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your crapples have been unlocked");
    }
}
