package com.playares.factions.listener;

import com.playares.commons.bukkit.timer.Timer;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.players.dao.PlayerDAO;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.util.FactionUtils;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class DataListener implements Listener {
    @Getter public final Factions plugin;

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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer != null && factionPlayer.isResetOnJoin()) {
            FactionUtils.resetPlayer(getPlugin(), player);
            factionPlayer.setResetOnJoin(false);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        new Scheduler(plugin).async(() -> {
            PlayerDAO.savePlayer(plugin.getMongo(), profile);
            plugin.getPlayerManager().getPlayerRepository().remove(profile);
        }).run();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null || profile.getTimers().isEmpty()) {
            return;
        }

        profile.getTimers().forEach(Timer::onFinish);
        profile.getTimers().clear();
    }
}