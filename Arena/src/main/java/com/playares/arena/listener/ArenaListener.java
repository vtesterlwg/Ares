package com.playares.arena.listener;

import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

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
}