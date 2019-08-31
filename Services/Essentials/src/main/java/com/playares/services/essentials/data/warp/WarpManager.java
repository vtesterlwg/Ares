package com.playares.services.essentials.data.warp;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Set;

public final class WarpManager {
    @Getter public final AresPlugin plugin;
    @Getter public final Set<Warp> warps;

    public WarpManager(AresPlugin plugin) {
        this.plugin = plugin;
        this.warps = Sets.newConcurrentHashSet();

        plugin.registerListener(new WarpSignListener(this));
    }

    public void load() {
        final YamlConfiguration config = getPlugin().getConfig("warps");

        if (!warps.isEmpty()) {
            warps.clear();
            Logger.warn("Cleared warps while reloading " + getPlugin().getName());
        }

        if (config.getConfigurationSection("warps") == null) {
            Logger.warn("No warps found...");
            return;
        }

        for (String warpNames : config.getConfigurationSection("warps").getKeys(false)) {
            final double x = config.getDouble("warps." + warpNames + ".x");
            final double y = config.getDouble("warps." + warpNames + ".y");
            final double z = config.getDouble("warps." + warpNames + ".z");
            final float yaw = (float)config.getDouble("warps." + warpNames + ".yaw");
            final float pitch = (float)config.getDouble("warps." + warpNames + ".pitch");
            final String worldName = config.getString("warps." + warpNames + ".world");

            final Warp warp = new Warp(warpNames, worldName, x, y, z, yaw, pitch);
            warps.add(warp);
        }
    }

    public Warp getWarp(String name) {
        return warps.stream().filter(warp -> warp.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void deleteWarp(Warp warp) {
        final YamlConfiguration config = getPlugin().getConfig("warps");
        config.set("warps." + warp.getName(), null);
        plugin.saveConfig("warps", config);
    }

    public void saveWarps() {
        final YamlConfiguration config = getPlugin().getConfig("warps");

        if (warps.isEmpty()) {
            config.set("warps", null);
            plugin.saveConfig("warps", config);
            return;
        }

        for (Warp warp : warps) {
            config.set("warps." + warp.getName() + ".x", warp.getX());
            config.set("warps." + warp.getName() + ".y", warp.getY());
            config.set("warps." + warp.getName() + ".z", warp.getZ());
            config.set("warps." + warp.getName() + ".yaw", warp.getYaw());
            config.set("warps." + warp.getName() + ".pitch", warp.getPitch());
            config.set("warps." + warp.getName() + ".world", warp.getWorldName());
        }

        plugin.saveConfig("warps", config);
    }
}
