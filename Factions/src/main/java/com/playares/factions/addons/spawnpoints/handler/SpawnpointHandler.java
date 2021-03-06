package com.playares.factions.addons.spawnpoints.handler;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.addons.spawnpoints.data.Spawnpoint;
import com.playares.factions.addons.spawnpoints.manager.SpawnpointManager;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class SpawnpointHandler {
    @Getter
    public final SpawnpointManager manager;

    public SpawnpointHandler(SpawnpointManager manager) {
        this.manager = manager;
    }

    public void teleport(Player player, SimplePromise promise) {
        if (!player.hasPermission("factions.spawn")) {
            promise.failure("Teleporting to Spawn has been disabled. You must run to X: 0, Z: 0, in the Overworld");
            return;
        }

        final Spawnpoint spawn = manager.getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD);

        if (spawn == null) {
            promise.failure("Spawn not set");
            return;
        }

        player.teleport(spawn.getBukkit());
        promise.success();
    }

    public void teleport(Player player, String name, SimplePromise promise) {
        if (!player.hasPermission("factions.spawn")) {
            promise.failure("You do not have permission to perform this action");
            return;
        }

        final Spawnpoint.SpawnpointType type = Spawnpoint.SpawnpointType.getType(name);

        if (type == null) {
            promise.failure("Spawnpoint type not found");
            return;
        }

        final Spawnpoint spawn = manager.getSpawnpoint(type);

        if (spawn == null) {
            promise.failure("Spawn not set");
            return;
        }

        player.teleport(spawn.getBukkit());
        promise.success();
    }

    public void setSpawn(Player player, String name, SimplePromise promise) {
        final Spawnpoint.SpawnpointType type = Spawnpoint.SpawnpointType.getType(name);

        if (type == null) {
            promise.failure("Invalid spawnpoint type");
            return;
        }

        setSpawn(new PLocatable(player), type);
        Logger.print(player.getName() + " updated spawnpoint for " + type.name());
        promise.success();
    }

    private void setSpawn(PLocatable locatable, Spawnpoint.SpawnpointType type) {
        final Spawnpoint existing = manager.getSpawnpoint(type);

        if (existing != null) {
            existing.setX(locatable.getX());
            existing.setY(locatable.getY());
            existing.setZ(locatable.getZ());
            existing.setYaw(locatable.getYaw());
            existing.setPitch(locatable.getPitch());
            existing.setWorldName(locatable.getWorldName());
            manager.saveSpawns();
            return;
        }

        final Spawnpoint spawnpoint = new Spawnpoint(type, locatable);
        manager.getSpawnpoints().add(spawnpoint);
        manager.saveSpawns();
    }
}