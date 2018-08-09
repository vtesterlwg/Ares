package com.playares.services.essentials.data.warp;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Set;

public final class WarpManager {
    @Getter
    public final AresPlugin plugin;

    @Getter
    public final Set<Warp> warps;

    @Getter
    public final YamlConfiguration warpsConfig;

    public WarpManager(AresPlugin plugin) {
        this.plugin = plugin;
        this.warps = Sets.newConcurrentHashSet();
        this.warpsConfig = plugin.getConfig("warps");

        if (warpsConfig.getConfigurationSection("warps") == null) {
            Logger.warn("No warps found...");
            return;
        }

        for (String warpNames : warpsConfig.getConfigurationSection("warps").getKeys(false)) {
            final double x = warpsConfig.getDouble("warps." + warpNames + ".x");
            final double y = warpsConfig.getDouble("warps." + warpNames + ".y");
            final double z = warpsConfig.getDouble("warps." + warpNames + ".z");
            final float yaw = (float)warpsConfig.getDouble("warps." + warpNames + ".yaw");
            final float pitch = (float)warpsConfig.getDouble("warps." + warpNames + ".pitch");
            final String worldName = warpsConfig.getString("warps." + warpNames + ".world");

            final Warp warp = new Warp(warpNames, worldName, x, y, z, yaw, pitch);
            this.warps.add(warp);
        }
    }

    public Warp getWarp(String name) {
        return warps.stream().filter(warp -> warp.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void deleteWarp(Warp warp) {
        warpsConfig.set("warps." + warp.getName(), null);
        plugin.saveConfig("warps", warpsConfig);
    }

    public void saveWarps() {
        if (warps.isEmpty()) {
            warpsConfig.set("warps", null);
            plugin.saveConfig("warps", warpsConfig);
            return;
        }

        for (Warp warp : warps) {
            warpsConfig.set("warps." + warp.getName() + ".x", warp.getX());
            warpsConfig.set("warps." + warp.getName() + ".y", warp.getY());
            warpsConfig.set("warps." + warp.getName() + ".z", warp.getZ());
            warpsConfig.set("warps." + warp.getName() + ".yaw", warp.getYaw());
            warpsConfig.set("warps." + warp.getName() + ".pitch", warp.getPitch());
            warpsConfig.set("warps." + warp.getName() + ".world", warp.getWorldName());
        }

        plugin.saveConfig("warps", warpsConfig);
    }
}
