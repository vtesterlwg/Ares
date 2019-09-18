package com.playares.arena.listener;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@AllArgsConstructor
public final class SpectatorListener implements Listener {
    @Getter public final Arenas plugin;

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity().getShooter();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (!profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileCollideEvent(ProjectileCollideEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getCollidedWith() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getCollidedWith();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null || !profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            event.setCancelled(true);
        }
    }
}