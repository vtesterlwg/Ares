package com.riotmc.factions.listener;

import com.riotmc.factions.Factions;
import com.riotmc.factions.players.FactionPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class PillarListener implements Listener {
    @Getter
    public Factions plugin;

    public PillarListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null) {
            profile.hideAllPillars();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null) {
            profile.hideAllPillars();
        }
    }
}