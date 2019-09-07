package com.playares.arena.arena.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public final class ArenaBuilderListener implements Listener {
    @Getter public final ArenaBuilderManager manager;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final ArenaBuilder builder = manager.getBuilder(player);

        if (builder != null) {
            manager.getBuilders().remove(builder);
        }
    }
}