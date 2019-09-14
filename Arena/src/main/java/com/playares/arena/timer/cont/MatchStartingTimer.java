package com.playares.arena.timer.cont;

import com.playares.arena.timer.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class MatchStartingTimer extends PlayerTimer {
    public MatchStartingTimer(UUID owner, int seconds) {
        super(owner, PlayerTimerType.MATCH_STARTING, seconds);
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Match Started!");
        }
    }
}
