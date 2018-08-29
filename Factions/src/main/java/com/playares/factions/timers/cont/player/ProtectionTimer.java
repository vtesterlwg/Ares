package com.playares.factions.timers.cont.player;

import com.playares.factions.timers.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ProtectionTimer extends PlayerTimer {
    public ProtectionTimer(UUID owner, int seconds) {
        super(owner, PlayerTimerType.PROTECTION, seconds);
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Your PvP Protection has expired");
    }
}
