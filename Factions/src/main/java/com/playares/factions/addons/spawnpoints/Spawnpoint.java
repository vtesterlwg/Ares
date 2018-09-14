package com.playares.factions.addons.spawnpoints;

import com.playares.commons.bukkit.location.PLocatable;
import lombok.Getter;

import javax.annotation.Nonnull;

public final class Spawnpoint extends PLocatable {
    @Getter
    public final SpawnpointType type;

    @SuppressWarnings("ConstantConditions") // hehe OOPSIE
    public Spawnpoint(@Nonnull SpawnpointType type, @Nonnull PLocatable locatable) {
        super(locatable.getWorldName(), locatable.getX(), locatable.getY(), locatable.getZ(), locatable.getYaw(), locatable.getPitch());
        this.type = type;
    }

    public Spawnpoint(@Nonnull SpawnpointType type, @Nonnull String worldName, double x, double y, double z, float yaw, float pitch) {
        super(worldName, x, y, z, yaw, pitch);
        this.type = type;
    }

    public enum SpawnpointType {
        OVERWORLD, END_ENTRANCE, END_CITY_ENTRANCE
    }
}