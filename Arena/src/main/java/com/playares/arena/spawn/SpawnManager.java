package com.playares.arena.spawn;

import com.playares.arena.Arenas;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class SpawnManager {
    @Getter public final Arenas plugin;
    @Getter public final SpawnHandler handler;
    @Getter @Setter public PLocatable spawn;

    public SpawnManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new SpawnHandler(this);
    }

    public void load() {
        final YamlConfiguration config = plugin.getConfig("config");

        if (config == null) {
            Logger.error("Failed to find config.yml");
            return;
        }

        final double x = config.getDouble("spawn.x");
        final double y = config.getDouble("spawn.y");
        final double z = config.getDouble("spawn.z");
        final float yaw = (float)config.getDouble("spawn.yaw");
        final float pitch = (float)config.getDouble("spawn.pitch");
        final String world = config.getString("spawn.world");

        final PLocatable location = new PLocatable(world, x, y, z, yaw, pitch);
        setSpawn(location);
    }

    public void save() {
        final YamlConfiguration config = plugin.getConfig("config");

        if (config == null) {
            Logger.error("Failed to find config.yml");
            return;
        }

        config.set("spawn.x", spawn.getX());
        config.set("spawn.y", spawn.getY());
        config.set("spawn.z", spawn.getZ());
        config.set("spawn.yaw", spawn.getYaw());
        config.set("spawn.pitch", spawn.getPitch());

        plugin.saveConfig("config", config);
        Logger.print("Saved spawn location to config");
    }
}