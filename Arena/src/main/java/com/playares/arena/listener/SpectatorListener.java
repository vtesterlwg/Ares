package com.playares.arena.listener;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.bukkit.event.PlayerLingeringSplashPlayerEvent;
import com.playares.commons.bukkit.event.PlayerSplashPlayerEvent;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

public final class SpectatorListener implements Listener {
    @Getter public final Arenas plugin;

    public SpectatorListener(Arenas arenaPlugin) {
        this.plugin = arenaPlugin;
    }

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
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            event.setCancelled(true);
            return;
        }

        if (!profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
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

    @EventHandler
    public void onPotionSplash(PlayerSplashPlayerEvent event) {
        final Player damaged = event.getDamaged();
        final Player damager = event.getDamager();
        final ArenaPlayer damagedProfile = plugin.getPlayerManager().getPlayer(damaged);
        final ArenaPlayer damagerProfile = plugin.getPlayerManager().getPlayer(damager);

        if (damagedProfile == null || damagerProfile == null) {
            event.setCancelled(true);
            return;
        }

        if (!damagerProfile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME) || !damagedProfile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLingeringSplash(PlayerLingeringSplashPlayerEvent event) {
        final Player damaged = event.getDamaged();
        final Player damager = event.getDamager();
        final ArenaPlayer damagedProfile = plugin.getPlayerManager().getPlayer(damaged);
        final ArenaPlayer damagerProfile = plugin.getPlayerManager().getPlayer(damager);

        if (damagedProfile == null || damagerProfile == null) {
            event.setCancelled(true);
            return;
        }

        if (!damagerProfile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME) || !damagedProfile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            event.setCancelled(true);
        }
    }
}