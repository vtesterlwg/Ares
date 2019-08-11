package com.playares.factions.listener;

import com.playares.factions.Factions;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.util.FactionUtils;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerListener implements Listener {
    @Getter public Factions plugin;

    public PlayerListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        event.setJoinMessage(null);

        if (faction != null) {
            player.setScoreboard(faction.getScoreboard());

            faction.registerFriendly(player);
            faction.sendMessage(ChatColor.YELLOW + "Member " + ChatColor.GREEN + "Online" + ChatColor.YELLOW + ": " + ChatColor.RESET + player.getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        event.setQuitMessage(null);

        if (faction != null) {
            faction.unregister(player);
            faction.sendMessage(ChatColor.YELLOW + "Member " + ChatColor.RED + "Offline" + ChatColor.YELLOW + ": " + ChatColor.RESET + player.getName());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        FactionUtils.resetPlayer(plugin, player);
    }
}