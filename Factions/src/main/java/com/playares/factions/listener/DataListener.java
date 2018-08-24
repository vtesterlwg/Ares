package com.playares.factions.listener;

import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.players.PlayerDAO;
import com.playares.factions.timers.cont.player.EnderpearlTimer;
import lombok.Getter;
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
        final FactionPlayer profile = plugin.getPlayerManager().loadPlayer(event.getUniqueId(), event.getName());
        profile.getTimers().add(new EnderpearlTimer(profile.getUniqueId(), 30)); // TODO: Debug, remove when finished
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

        new Scheduler(plugin).async(() -> PlayerDAO.savePlayer(plugin.getMongo(), profile)).run();
    }
}