package com.playares.arena.arena;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.arena.builder.ArenaBuilderManager;
import com.playares.arena.arena.data.Arena;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Set;
import java.util.stream.Collectors;

public final class ArenaManager {
    @Getter public Arenas plugin;
    @Getter public ArenaHandler handler;
    @Getter public final ArenaBuilderManager builderManager;
    @Getter public final Set<Arena> arenas;

    public ArenaManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new ArenaHandler(this);
        this.builderManager = new ArenaBuilderManager(this);
        this.arenas = Sets.newConcurrentHashSet();
    }

    public void load() {
        final YamlConfiguration config = getPlugin().getConfig("arenas");

        if (config == null) {
            Logger.error("Failed to obtain arenas.yml while loading Arenas");
            return;
        }

        if (arenas.isEmpty()) {
            Logger.warn("Clearing existing arenas from memory while reloading data");
        }

        for (String arenaName : config.getConfigurationSection("arenas").getKeys(false)) {
            final String path = "arenas." + arenaName + ".";
            final String displayName = ChatColor.translateAlternateColorCodes('&', config.getString(path + "display-name"));

            final PLocatable spawnA = new PLocatable(
                    config.getString(path + "spawnpoints.a.world"),
                    config.getDouble(path + "spawnpoints.a.x"),
                    config.getDouble(path + "spawnpoints.a.y"),
                    config.getDouble(path + "spawnpoints.a.z"),
                    (float)config.getDouble(path + "spawnpoints.a.yaw"),
                    (float)config.getDouble(path + "spawnpoints.a.pitch")
            );

            final PLocatable spawnB = new PLocatable(
                    config.getString(path + "spawnpoints.b.world"),
                    config.getDouble(path + "spawnpoints.b.x"),
                    config.getDouble(path + "spawnpoints.b.y"),
                    config.getDouble(path + "spawnpoints.b.z"),
                    (float)config.getDouble(path + "spawnpoints.b.yaw"),
                    (float)config.getDouble(path + "spawnpoints.b.pitch")
            );

            final PLocatable spawnSpec = new PLocatable(
                    config.getString(path + "spawnpoints.spec.world"),
                    config.getDouble(path + "spawnpoints.spec.x"),
                    config.getDouble(path + "spawnpoints.spec.y"),
                    config.getDouble(path + "spawnpoints.spec.z"),
                    (float)config.getDouble(path + "spawnpoints.spec.yaw"),
                    (float)config.getDouble(path + "spawnpoints.spec.pitch")
            );

            final Arena arena = new Arena(plugin, arenaName, displayName, spawnA, spawnB, spawnSpec);
            arenas.add(arena);
        }

        Logger.print("Loaded " + arenas.size() + " Arenas");
    }

    public Arena obtainArena() {
        final Set<Arena> available = getAvailableArenas();

        if (available.isEmpty()) {
            return null;
        }

        final Arena arena = available.stream().findFirst().orElse(null);
        arena.setInUse(true);

        return arena;
    }

    public ImmutableSet<Arena> getAvailableArenas() {
        return ImmutableSet.copyOf(arenas.stream().filter(arena -> !arena.isInUse()).collect(Collectors.toSet()));
    }

    public Arena getArena(String name) {
        return arenas.stream().filter(arena -> arena.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}