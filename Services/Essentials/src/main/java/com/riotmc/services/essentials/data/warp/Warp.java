package com.riotmc.services.essentials.data.warp;

import com.playares.commons.bukkit.location.PLocatable;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class Warp extends PLocatable {
    @Getter
    public final String name;

    public Warp(String name, String worldName, double x, double y, double z, float yaw, float pitch) {
        super(worldName, x, y, z, yaw, pitch);
        this.name = name;
    }

    public void teleport(Player player) {
        player.teleport(getBukkit());
    }
}
