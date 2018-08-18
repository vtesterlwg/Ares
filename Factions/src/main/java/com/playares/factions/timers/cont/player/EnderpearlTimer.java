package com.playares.factions.timers.cont.player;

import com.playares.factions.timers.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.UUID;

public final class EnderpearlTimer extends PlayerTimer {
    public EnderpearlTimer(UUID owner, int seconds) {
        super(owner, PlayerTimerType.ENDERPEARL, seconds);
    }

    @Override
    public void onFinish() {
        if (Bukkit.getPlayer(owner) != null) {
            Bukkit.getPlayer(owner).sendMessage(ChatColor.GREEN + "Your enderpearls have been unlocked");
        }
    }
}
