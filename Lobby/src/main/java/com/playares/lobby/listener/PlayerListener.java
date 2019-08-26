package com.playares.lobby.listener;

import com.playares.commons.bukkit.util.Players;
import com.playares.lobby.Lobby;
import com.playares.lobby.items.ServerSelectorItem;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerListener implements Listener {
    @Getter public final Lobby plugin;

    public PlayerListener(Lobby plugin) {
        this.plugin = plugin;
    }

    private void processBlockEvent(Player player, Cancellable event) {
        if (player.hasPermission("lobby.edit")) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final CustomItemService customItemService = (CustomItemService)getPlugin().getService(CustomItemService.class);

        Players.resetHealth(player);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        Players.sendTablist(getPlugin().getProtocol(), player, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Ares Network", ChatColor.AQUA + "playares.net");

        if (customItemService != null) {
            customItemService.getItem(ServerSelectorItem.class).ifPresent(selector -> player.getInventory().addItem(selector.getItem()));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        processBlockEvent(event.getPlayer(), event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        processBlockEvent(event.getPlayer(), event);
    }
}