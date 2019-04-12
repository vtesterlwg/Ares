package com.playares.arena.listener;

import com.destroystokyo.paper.Title;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

        new Scheduler(plugin).sync(() -> bukkitPlayer.sendTitle(new Title(ChatColor.GOLD + "Welcome to the Arena!", ChatColor.DARK_RED + "Good Luck and Have fun!", 20, 60, 20))).delay(10L).run();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player bukkitPlayer = event.getPlayer();
        final ArenaPlayer player = plugin.getPlayerManager().getPlayer(bukkitPlayer);

        if (player == null) {
            Logger.error("Could not find profile for " + bukkitPlayer.getName() + "!");
            return;
        }

        final Team team = plugin.getTeamManager().getTeam(player);

        // TODO: Kill player in match, Update teams

        plugin.getPlayerManager().getPlayers().remove(player);
    }
}
