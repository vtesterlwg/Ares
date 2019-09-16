package com.playares.arena.listener;

import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.services.playerclasses.event.PlayerClassDeactivateEvent;
import com.playares.services.playerclasses.event.PlayerClassReadyEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AllArgsConstructor
public final class ClassListener implements Listener {
    @Getter public final Arenas plugin;

    @EventHandler
    public void onClassReady(PlayerClassReadyEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null) {
            return;
        }

        if (profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            event.getPlayerClass().activate(player, false);
        }
    }

    @EventHandler
    public void onClassDeactivate(PlayerClassDeactivateEvent event) {
        event.setMessage(false);
    }
}