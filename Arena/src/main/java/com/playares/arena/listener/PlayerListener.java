package com.playares.arena.listener;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.PlayerStatus;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.util.Players;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import javax.annotation.Nonnull;

public final class PlayerListener implements Listener {
    @Nonnull @Getter
    public final Arenas plugin;

    public PlayerListener(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        event.setJoinMessage(null);

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Could not find your profile");
            return;
        }

        Players.resetHealth(player);
        Players.sendTablist(plugin.getProtocol(), player, ChatColor.GOLD + "" + ChatColor.BOLD + "Ares Network", ChatColor.GOLD + "playares.com");

        player.teleport(plugin.getPlayerHandler().getLobby().getBukkit());
        player.sendTitle(ChatColor.DARK_RED + "Welcome to the Arena!", ChatColor.GOLD + "Good luck and have fun!", 5, 40, 5);

        plugin.getPlayerHandler().giveLobbyItems(profile);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        event.setQuitMessage(null);

        if (profile == null) {
            return;
        }

        if (profile.getTeam() != null) {
            plugin.getTeamHandler().leaveTeam(profile, new SimplePromise() {
                @Override
                public void success() {}

                @Override
                public void failure(@Nonnull String reason) {}
            });
        }

        if (profile.getMatch() != null) {
            profile.getMatch().getSpectators().remove(profile);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(plugin.getPlayerHandler().getLobby().getBukkit());
    }

    @EventHandler
    public void onProjectileCollide(ProjectileCollideEvent event) {
        if (!(event.getCollidedWith() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getCollidedWith();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (profile.getStatus().equals(PlayerStatus.INGAME)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getWhoClicked();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (profile.getStatus().equals(PlayerStatus.INGAME)) {
            return;
        }

        if (player.hasPermission("arena.admin")) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (profile.getStatus().equals(PlayerStatus.INGAME)) {
            if (player.getInventory().getHeldItemSlot() == 0) {
                player.sendMessage(ChatColor.RED + "You can not drop items in your 1 slot");
                event.setCancelled(true);
            }

            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (profile.getStatus().equals(PlayerStatus.INGAME)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (!profile.getStatus().equals(PlayerStatus.LOBBY)) {
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("arena.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (!profile.getStatus().equals(PlayerStatus.LOBBY)) {
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("arena.admin")) {
            event.setCancelled(true);
        }
    }
}