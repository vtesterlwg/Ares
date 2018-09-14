package com.playares.factions.addons.spawnpoints;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Set;

public final class SpawnpointManager {
    @Getter
    public final Factions plugin;

    @Getter
    public final SpawnpointHandler handler;

    @Getter
    public final Set<Spawnpoint> spawnpoints;

    public SpawnpointManager(Factions plugin) {
        this.plugin = plugin;
        this.handler = new SpawnpointHandler(this);
        this.spawnpoints = Sets.newHashSet();
    }

    public Spawnpoint getSpawnpoint(Spawnpoint.SpawnpointType type) {
        return spawnpoints.stream().filter(spawn -> spawn.getType().equals(type)).findFirst().orElse(null);
    }

    public void loadSpawns() {
        final YamlConfiguration config = plugin.getConfig("spawnpoints");

        if (config.get("overworld") != null) {
            final Spawnpoint spawn = new Spawnpoint(Spawnpoint.SpawnpointType.OVERWORLD,
                    config.getString("overworld.world"),
                    config.getDouble("overworld.x"),
                    config.getDouble("overworld.y"),
                    config.getDouble("overworld.z"),
                    (float)config.getDouble("overworld.yaw"),
                    (float)config.getDouble("overworld.pitch"));

            spawnpoints.add(spawn);
        }

        if (config.get("end-entrance") != null) {
            final Spawnpoint spawn = new Spawnpoint(Spawnpoint.SpawnpointType.END_ENTRANCE,
                    config.getString("end-entrance.world"),
                    config.getDouble("end-entrance.x"),
                    config.getDouble("end-entrance.y"),
                    config.getDouble("end-entrance.z"),
                    (float)config.getDouble("end-entrance.yaw"),
                    (float)config.getDouble("end-entrance.pitch"));

            spawnpoints.add(spawn);
        }

        Logger.print("Loaded " + spawnpoints.size() + " Spawnpoints");
    }

    public void saveSpawns() {
        final YamlConfiguration config = plugin.getConfig("spawnpoints");

        spawnpoints.forEach(spawn -> {
            final String fixedName = spawn.getType().name().toLowerCase().replace("_", "-");
            config.set(fixedName + ".x", spawn.getX());
            config.set(fixedName + ".y", spawn.getY());
            config.set(fixedName + ".z", spawn.getZ());
            config.set(fixedName + ".yaw", spawn.getYaw());
            config.set(fixedName + ".pitch", spawn.getPitch());
            config.set(fixedName + ".world", spawn.getWorldName());
        });

        plugin.saveConfig("spawnpoints", config);
        Logger.print("Saved " + spawnpoints.size() + " Spawnpoints");
    }
}