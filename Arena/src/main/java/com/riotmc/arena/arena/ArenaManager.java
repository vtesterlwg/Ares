package com.riotmc.arena.arena;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.arena.cont.MainArena;
import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.commons.bukkit.logger.Logger;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public final class ArenaManager {
    @Nonnull @Getter
    public final Arenas plugin;

    @Nonnull @Getter
    public final Set<Arena> arenas;

    @Nonnull @Getter
    public final YamlConfiguration config;

    public ArenaManager(@Nonnull Arenas plugin) {
        this.plugin = plugin;
        this.arenas = Sets.newCopyOnWriteArraySet();
        this.config = plugin.getConfig("arenas");

        for (String arenaName : config.getConfigurationSection("arenas").getKeys(false)) {
            final List<String> authors = config.getStringList("arenas." + arenaName + ".authors");
            final List<PLocatable> spawns = Lists.newArrayList();

            final double aX = config.getDouble("arenas." + arenaName + ".spawns.a.x");
            final double aY = config.getDouble("arenas." + arenaName + ".spawns.a.y");
            final double aZ = config.getDouble("arenas." + arenaName + ".spawns.a.z");
            final float aYaw = (float)config.getDouble("arenas." + arenaName + ".spawns.a.yaw");
            final float aPitch = (float)config.getDouble("arenas." + arenaName + ".spawns.a.pitch");
            final String aWorld = config.getString("arenas." + arenaName + ".spawns.a.world");

            final double bX = config.getDouble("arenas." + arenaName + ".spawns.b.x");
            final double bY = config.getDouble("arenas." + arenaName + ".spawns.b.y");
            final double bZ = config.getDouble("arenas." + arenaName + ".spawns.b.z");
            final float bYaw = (float)config.getDouble("arenas." + arenaName + ".spawns.b.yaw");
            final float bPitch = (float)config.getDouble("arenas." + arenaName + ".spawns.b.pitch");
            final String bWorld = config.getString("arenas." + arenaName + ".spawns.b.world");

            final PLocatable spawnA = new PLocatable(aWorld, aX, aY, aZ, aYaw, aPitch);
            final PLocatable spawnB = new PLocatable(bWorld, bX, bY, bZ, bYaw, bPitch);

            spawns.add(spawnA);
            spawns.add(spawnB);

            final MainArena arena = new MainArena(arenaName, authors, spawns);
            arenas.add(arena);
        }

        Logger.print("Loaded " + arenas.size() + " Arenas");
    }

    @Nullable
    public Arena getArena(String name) {
        return arenas.stream().filter(arena -> arena.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Nonnull
    public ImmutableList<Arena> getAvailableArenas() {
        return ImmutableList.copyOf(arenas.stream().filter(arena -> !arena.isInUse()).collect(Collectors.toList()));
    }

    @Nonnull
    public ImmutableList<String> getArenaList() {
        final List<String> names = Lists.newArrayList();
        arenas.forEach(arena -> names.add(arena.getName()));
        return ImmutableList.copyOf(names);
    }

    @Nullable
    public Arena getRandomArena() {
        final List<Arena> available = getAvailableArenas();

        if (available.isEmpty()) {
            return null;
        }

        return available.get(new Random().nextInt(available.size()));
    }

    public void saveArena(@Nonnull Arena arena) {
        if (!arena.isConfigured()) {
            return;
        }

        config.set("arenas." + arena.getName() + ".authors", arena.getAuthors());

        config.set("arenas." + arena.getName() + ".spawns.a.x", arena.getSpawns().get(0).getX());
        config.set("arenas." + arena.getName() + ".spawns.a.y", arena.getSpawns().get(0).getY());
        config.set("arenas." + arena.getName() + ".spawns.a.z", arena.getSpawns().get(0).getZ());
        config.set("arenas." + arena.getName() + ".spawns.a.yaw", arena.getSpawns().get(0).getYaw());
        config.set("arenas." + arena.getName() + ".spawns.a.pitch", arena.getSpawns().get(0).getPitch());
        config.set("arenas." + arena.getName() + ".spawns.a.world", arena.getSpawns().get(0).getWorldName());

        config.set("arenas." + arena.getName() + ".spawns.b.x", arena.getSpawns().get(1).getX());
        config.set("arenas." + arena.getName() + ".spawns.b.y", arena.getSpawns().get(1).getY());
        config.set("arenas." + arena.getName() + ".spawns.b.z", arena.getSpawns().get(1).getZ());
        config.set("arenas." + arena.getName() + ".spawns.b.yaw", arena.getSpawns().get(1).getYaw());
        config.set("arenas." + arena.getName() + ".spawns.b.pitch", arena.getSpawns().get(1).getPitch());
        config.set("arenas." + arena.getName() + ".spawns.b.world", arena.getSpawns().get(1).getWorldName());

        plugin.saveConfig("arenas", config);
    }

    public void deleteArena(@Nonnull Arena arena) {
        config.set("arenas." + arena.getName(), null);
        plugin.saveConfig("arenas", config);
    }
}