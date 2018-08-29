package com.playares.arena.listener;

import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nonnull;

public final class DataListener implements Listener {
    @Nonnull @Getter
    public final Arenas plugin;

    public DataListener(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer arenaPlayer = new ArenaPlayer(player);

        plugin.getPlayerManager().getPlayers().add(arenaPlayer);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final ArenaPlayer player = plugin.getPlayerManager().getPlayer(event.getPlayer().getUniqueId());

        if (player == null) {
            return;
        }

        plugin.getPlayerManager().getPlayers().remove(player);
    }
}
