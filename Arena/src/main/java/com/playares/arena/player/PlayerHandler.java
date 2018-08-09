package com.playares.arena.player;

import com.playares.arena.Arenas;
import com.playares.arena.items.CreateTeamItem;
import com.playares.arena.items.LeaveTeamItem;
import com.playares.arena.items.ViewTeamItem;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class PlayerHandler {
    @Getter
    public final Arenas plugin;

    @Getter
    public PLocatable lobby;

    public PlayerHandler(Arenas plugin) {
        this.plugin = plugin;

        if (plugin.getMainConfig().get("locations.lobby") != null) {
            this.lobby = new PLocatable(
                    plugin.getMainConfig().getString("locations.lobby.world"),
                    plugin.getMainConfig().getDouble("locations.lobby.x"),
                    plugin.getMainConfig().getDouble("locations.lobby.y"),
                    plugin.getMainConfig().getDouble("locations.lobby.z"),
                    (float)plugin.getMainConfig().getDouble("locations.lobby.yaw"),
                    (float)plugin.getMainConfig().getDouble("locations.lobby.pitch")
            );

            Logger.print("Loaded lobby location from file");
        } else {
            this.lobby = new PLocatable(Bukkit.getWorlds().get(0).getName(), 0.0, 65.0, 0.0, 0.0F, 0.0F);
            Logger.warn("Loaded default lobby location");
        }
    }

    public void giveLobbyItems(ArenaPlayer player) {
        final Player bukkit = player.getPlayer();
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        if (customItemService == null) {
            bukkit.sendMessage(ChatColor.RED + "Failed to retrieve lobby items");
            return;
        }

        Players.resetHealth(bukkit);
        bukkit.getInventory().clear();
        bukkit.getInventory().setArmorContents(null);

        new Scheduler(plugin).sync(() -> {
            if (player.getTeam() != null) {
                customItemService.getItem(ViewTeamItem.class).ifPresent(item -> bukkit.getInventory().setItem(4, item.getItem()));
                customItemService.getItem(LeaveTeamItem.class).ifPresent(item -> bukkit.getInventory().setItem(8, item.getItem()));

                return;
            }

            customItemService.getItem(CreateTeamItem.class).ifPresent(item -> bukkit.getInventory().setItem(4, item.getItem()));
        }).delay(1L).run();
    }

    public void setLobby(PLocatable location) {
        this.lobby = location;

        plugin.getMainConfig().set("locations.lobby.world", location.getWorldName());
        plugin.getMainConfig().set("locations.lobby.x", location.getX());
        plugin.getMainConfig().set("locations.lobby.y", location.getY());
        plugin.getMainConfig().set("locations.lobby.z", location.getZ());
        plugin.getMainConfig().set("locations.lobby.yaw", location.getYaw());
        plugin.getMainConfig().set("locations.lobby.pitch", location.getPitch());
        plugin.saveConfig("config", plugin.getMainConfig());

        Logger.print("Lobby location updated");
    }
}
