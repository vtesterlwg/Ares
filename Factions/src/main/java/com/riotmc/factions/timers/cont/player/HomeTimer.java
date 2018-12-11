package com.riotmc.factions.timers.cont.player;

import com.riotmc.commons.bukkit.util.Players;
import com.riotmc.factions.Factions;
import com.riotmc.factions.factions.PlayerFaction;
import com.riotmc.factions.timers.PlayerTimer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a players home timer
 */
public final class HomeTimer extends PlayerTimer {
    @Getter
    public final Factions plugin;

    @Getter
    public final PlayerFaction faction;

    public HomeTimer(Factions plugin, UUID owner, PlayerFaction faction, int seconds) {
        super(owner, PlayerTimerType.HOME, seconds);
        this.plugin = plugin;
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

        Players.teleportWithVehicle(plugin, player, faction.getHome().getBukkit());
        player.sendMessage(ChatColor.GREEN + "Returned to faction home");
    }
}