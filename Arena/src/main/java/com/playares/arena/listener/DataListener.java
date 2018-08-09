package com.playares.arena.listener;

import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class DataListener implements Listener {
    @Getter
    public final Arenas plugin;

    public DataListener(Arenas plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer arenaPlayer = new ArenaPlayer(player);

        plugin.getPlayerManager().getPlayers().add(arenaPlayer);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final ArenaPlayer player = plugin.getPlayerManager().getPlayer(event.getPlayer().getUniqueId());

        if (player == null) {
            return;
        }

        plugin.getPlayerManager().getPlayers().remove(player);
    }
}
