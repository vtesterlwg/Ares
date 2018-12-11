package com.riotmc.factions.listener;

import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.Factions;
import com.riotmc.factions.players.FactionPlayer;
import com.riotmc.factions.players.PlayerDAO;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class DataListener implements Listener {
    @Getter
    public final Factions plugin;

    public DataListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (plugin.getPlayerManager() == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "The server is still starting up");
            return;
        }

        final FactionPlayer profile = plugin.getPlayerManager().loadPlayer(event.getUniqueId(), event.getName());
        plugin.getPlayerManager().getPlayerRepository().add(profile);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        profile.getStats().addPlaytime(player.getLastPlayed());

        new Scheduler(plugin).async(() -> {
            PlayerDAO.savePlayer(plugin.getMongo(), profile);
            plugin.getPlayerManager().getPlayerRepository().remove(profile);
        }).run();
    }
}