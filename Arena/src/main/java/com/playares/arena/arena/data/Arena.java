package com.playares.arena.arena.data;

import com.playares.arena.Arenas;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class Arena {
    @Getter public Arenas plugin;
    @Getter public String name;
    @Getter public String displayName;
    @Getter public PLocatable spawnpointA;
    @Getter public PLocatable spawnpointB;
    @Getter public PLocatable spectatorSpawnpoint;
    @Getter @Setter public boolean inUse;

    public Arena(Arenas plugin, String name, String displayName, PLocatable spawnA, PLocatable spawnB, PLocatable spawnSpec) {
        this.plugin = plugin;
        this.name = name;
        this.displayName = displayName;
        this.spawnpointA = spawnA;
        this.spawnpointB = spawnB;
        this.spectatorSpawnpoint = spawnSpec;
    }

    public void teleportToSpawnpointA(Player player) {
        player.teleport(getSpawnpointA().getBukkit());
    }

    public void teleportToSpawnpointB(Player player) {
        player.teleport(getSpawnpointB().getBukkit());
    }

    public void teleportToSpectatorSpawnpoint(Player player) {
        player.teleport(getSpectatorSpawnpoint().getBukkit());
    }

    public void save() {
        final YamlConfiguration config = plugin.getConfig("arenas");

        config.set("arenas." + name + ".display-name", displayName);

        config.set("arenas." + name + ".spawnpoints.a.x", spawnpointA.getX());
        config.set("arenas." + name + ".spawnpoints.a.y", spawnpointA.getY());
        config.set("arenas." + name + ".spawnpoints.a.z", spawnpointA.getZ());
        config.set("arenas." + name + ".spawnpoints.a.yaw", spawnpointA.getYaw());
        config.set("arenas." + name + ".spawnpoints.a.pitch", spawnpointA.getPitch());
        config.set("arenas." + name + ".spawnpoints.a.world", spawnpointA.getWorldName());

        config.set("arenas." + name + ".spawnpoints.b.x", spawnpointB.getX());
        config.set("arenas." + name + ".spawnpoints.b.y", spawnpointB.getY());
        config.set("arenas." + name + ".spawnpoints.b.z", spawnpointB.getZ());
        config.set("arenas." + name + ".spawnpoints.b.yaw", spawnpointB.getYaw());
        config.set("arenas." + name + ".spawnpoints.b.pitch", spawnpointB.getPitch());
        config.set("arenas." + name + ".spawnpoints.b.world", spawnpointB.getWorldName());

        config.set("arenas." + name + ".spawnpoints.spec.x", spectatorSpawnpoint.getX());
        config.set("arenas." + name + ".spawnpoints.spec.y", spectatorSpawnpoint.getY());
        config.set("arenas." + name + ".spawnpoints.spec.z", spectatorSpawnpoint.getZ());
        config.set("arenas." + name + ".spawnpoints.spec.yaw", spectatorSpawnpoint.getYaw());
        config.set("arenas." + name + ".spawnpoints.spec.pitch", spectatorSpawnpoint.getPitch());
        config.set("arenas." + name + ".spawnpoints.spec.world", spectatorSpawnpoint.getWorldName());

        plugin.saveConfig("arenas", config);

        Logger.print("Saved Arena: " + name);
    }
}