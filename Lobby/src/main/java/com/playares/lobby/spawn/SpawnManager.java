package com.playares.lobby.spawn;

import com.playares.commons.bukkit.location.PLocatable;
import com.playares.lobby.Lobby;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class SpawnManager {
    @Getter public final Lobby plugin;
    @Getter @Setter public PLocatable spawn;
    @Getter public final SpawnHandler handler;

    public SpawnManager(Lobby plugin) {
        this.plugin = plugin;
        this.spawn = new PLocatable(Bukkit.getWorlds().get(0).getName(), 0.0, 100.0, 0.0, 0.0F, 0.0F);
        this.handler = new SpawnHandler(this);
    }

    public void load() {
        final YamlConfiguration config = getPlugin().getConfig("config");
        final double x = config.getDouble("spawn.x");
        final double y = config.getDouble("spawn.y");
        final double z = config.getDouble("spawn.z");
        final float yaw = (float)config.getDouble("spawn.yaw");
        final float pitch = (float)config.getDouble("spawn.pitch");
        final String world = config.getString("spawn.world");
        setSpawn(new PLocatable(world, x, y, z, yaw, pitch));
    }

    public void save() {
        final YamlConfiguration config = getPlugin().getConfig("config");
        config.set("spawn.x", spawn.getX());
        config.set("spawn.y", spawn.getY());
        config.set("spawn.z", spawn.getZ());
        config.set("spawn.yaw", spawn.getYaw());
        config.set("spawn.pitch", spawn.getPitch());
        config.set("spawn.world", spawn.getWorldName());
        getPlugin().saveConfig("config", config);
    }

    public void teleport(Player player) {
        player.teleport(getSpawn().getBukkit());
    }
}