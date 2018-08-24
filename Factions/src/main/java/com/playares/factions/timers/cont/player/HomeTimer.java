package com.playares.factions.timers.cont.player;

import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.timers.PlayerTimer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class HomeTimer extends PlayerTimer {
    @Getter
    public final PlayerFaction faction;

    public HomeTimer(UUID owner, PlayerFaction faction, int seconds) {
        super(owner, PlayerTimerType.HOME, seconds);
        this.faction = faction;
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        if (faction.getHome() == null) {
            player.sendMessage(ChatColor.RED + "Faction home not found (was it unset?)");
            return;
        }

        player.teleport(faction.getHome().getBukkit());
        player.sendMessage(ChatColor.GREEN + "Returned to faction home");
    }
}