package com.playares.arena.listener;

import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public final class ArenaListener implements Listener {
    @Getter public final Arenas plugin;

    private void handleBlockMods(Player player, Cancellable event) {
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("arena.admin")) {
            event.setCancelled(true);
            return;
        }

        if (!profile.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleBlockMods(event.getPlayer(), event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockMods(event.getPlayer(), event);
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null || !profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            player.setFoodLevel(20);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getWhoClicked();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (!event.getClickedInventory().equals(player.getInventory())) {
            return;
        }

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            return;
        }

        if (profile.getStatus().equals(ArenaPlayer.PlayerStatus.SPECTATING) || profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME_DEAD)) {
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("arena.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            return;
        }

        if (profile.getStatus().equals(ArenaPlayer.PlayerStatus.SPECTATING) || profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME_DEAD)) {
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("arena.admin")) {
            event.setCancelled(true);
        }
    }
}