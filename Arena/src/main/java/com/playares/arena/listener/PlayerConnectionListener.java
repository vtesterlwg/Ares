package com.playares.arena.listener;

import com.destroystokyo.paper.Title;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.ArenaPlayerDAO;
import com.playares.arena.queue.SearchingPlayer;
import com.playares.arena.team.Team;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class PlayerConnectionListener implements Listener {
    @Getter public Arenas plugin;

    public PlayerConnectionListener(Arenas plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();
        final String username = event.getName();
        final ArenaPlayer player = new ArenaPlayer(uniqueId, username);

        if (plugin.getPlayerManager().getPlayer(uniqueId) != null || plugin.getPlayerManager().getPlayer(username) != null) {
            return;
        }

        plugin.getPlayerManager().getPlayers().add(player);

        new Scheduler(plugin).async(() -> ArenaPlayerDAO.getRankedData(plugin.getMongo(), player)).run();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player bukkitPlayer = event.getPlayer();
        final ArenaPlayer player = plugin.getPlayerManager().getPlayer(bukkitPlayer);

        if (player == null) {
            bukkitPlayer.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        plugin.getPlayerManager().getHandler().giveItems(player);
        plugin.getSpawnManager().getHandler().teleport(bukkitPlayer);

        new Scheduler(plugin).sync(() -> bukkitPlayer.sendTitle(new Title("", ChatColor.GOLD + "Welcome to the Ares Arena!", 20, 60, 20))).delay(10L).run();
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player bukkitPlayer = event.getPlayer();
        final ArenaPlayer player = plugin.getPlayerManager().getPlayer(bukkitPlayer);

        if (player == null) {
            Logger.error("Could not find profile for " + bukkitPlayer.getName() + "!");
            return;
        }

        new Scheduler(plugin).async(() -> ArenaPlayerDAO.saveRankedData(plugin.getMongo(), player)).run();

        // Removing player from searches
        final SearchingPlayer search = plugin.getQueueManager().getCurrentSearch(bukkitPlayer);

        if (search != null) {
            plugin.getQueueManager().getSearchingPlayers().remove(search);
        }

        // Removing from team
        final Team team = plugin.getTeamManager().getTeam(player);

        if (player.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            player.setStatus(ArenaPlayer.PlayerStatus.INGAME_DEAD);
        }

        if (team != null) {
            team.sendMessage(ChatColor.AQUA + player.getUsername() + ChatColor.YELLOW + " has " + ChatColor.RED + "left" + ChatColor.YELLOW + " the team");

            if (team.isLeader(bukkitPlayer.getUniqueId())) {
                team.transferLeadership();
            } else {
                team.getMembers().remove(player);
            }
        }

        // Removing player profile
        plugin.getPlayerManager().getPlayers().remove(player);
    }
}
