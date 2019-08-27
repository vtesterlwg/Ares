package com.playares.factions.addons.spawnpoints;

import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.spawnpoints.command.SpawnCommand;
import com.playares.factions.addons.spawnpoints.listener.SpawnpointListener;
import com.playares.factions.addons.spawnpoints.manager.SpawnpointManager;
import lombok.Getter;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class SpawnpointAddon implements Addon {
    @Getter public final Factions plugin;
    @Getter public SpawnpointManager manager;
    @Getter public SpawnpointListener listener;

    public SpawnpointAddon(Factions plugin) {
        this.plugin = plugin;
        this.manager = new SpawnpointManager(plugin);
        this.listener = new SpawnpointListener(this);
    }

    @Override
    public String getName() {
        return "Spawnpoints";
    }

    @Override
    public void prepare() {
        manager.loadSpawns();
    }

    @Override
    public void start() {
        plugin.registerCommand(new SpawnCommand(plugin));
        plugin.registerListener(listener);
    }

    @Override
    public void stop() {
        PlayerPortalEvent.getHandlerList().unregister(listener);
        PlayerTeleportEvent.getHandlerList().unregister(listener);
        PlayerJoinEvent.getHandlerList().unregister(listener);
        PlayerRespawnEvent.getHandlerList().unregister(listener);
        manager.saveSpawns();
    }
}