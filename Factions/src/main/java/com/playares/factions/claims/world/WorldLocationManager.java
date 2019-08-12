package com.playares.factions.claims.world;

import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.location.Locatable;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.factions.Factions;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public final class WorldLocationManager {
    @Getter public final Factions plugin;

    @Getter public double overworldWarzoneRadius;
    @Getter public double netherWarzoneRadius;
    @Getter public double endRadius;

    public WorldLocationManager(Factions plugin) {
        this.plugin = plugin;
    }

    public void load() {
        final YamlConfiguration config = plugin.getConfig("config");

        this.overworldWarzoneRadius = config.getDouble("world-locations.overworld-warzone");
        this.netherWarzoneRadius = config.getDouble("world-locations.nether-warzone");
        this.endRadius = config.getDouble("world-locations.end");
    }

    public WorldLocation getWorldLocation(Locatable location) {
        final double x = Math.abs(location.getX());
        final double z = Math.abs(location.getZ());

        if (location instanceof PLocatable) {
            final PLocatable playerLocation = (PLocatable)location;

            if (playerLocation.getBukkit().getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                if (x <= overworldWarzoneRadius && z <= overworldWarzoneRadius) {
                    return WorldLocation.OVERWORLD_WARZONE;
                } else {
                    return WorldLocation.OVERWORLD_WILDERNESS;
                }
            }

            if (playerLocation.getBukkit().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                if (x <= netherWarzoneRadius && z <= netherWarzoneRadius) {
                    return WorldLocation.NETHER_WARZONE;
                } else {
                    return WorldLocation.NETHER_WILDERNESS;
                }
            }

            if (playerLocation.getBukkit().getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                if (x <= endRadius && z <= endRadius) {
                    return WorldLocation.THE_END;
                } else {
                    return WorldLocation.THE_END_CITY;
                }
            }
        }

        if (location instanceof BLocatable) {
            final BLocatable blockLocation = (BLocatable) location;

            if (blockLocation.getBukkit().getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                if (x <= overworldWarzoneRadius && z <= overworldWarzoneRadius) {
                    return WorldLocation.OVERWORLD_WARZONE;
                } else {
                    return WorldLocation.OVERWORLD_WILDERNESS;
                }
            }

            if (blockLocation.getBukkit().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                if (x <= netherWarzoneRadius && z <= netherWarzoneRadius) {
                    return WorldLocation.NETHER_WARZONE;
                } else {
                    return WorldLocation.NETHER_WILDERNESS;
                }
            }

            if (blockLocation.getBukkit().getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                if (x <= endRadius && z <= endRadius) {
                    return WorldLocation.THE_END;
                } else {
                    return WorldLocation.THE_END_CITY;
                }
            }
        }

        return WorldLocation.UNKNOWN;
    }
}