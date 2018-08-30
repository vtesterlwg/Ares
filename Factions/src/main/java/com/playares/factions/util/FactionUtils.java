package com.playares.factions.util;

import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import org.bukkit.entity.Player;

public final class FactionUtils {
    public static void teleportOutsideClaims(Factions plugin, Player player) {
        final PLocatable location = new PLocatable(player);

        new Scheduler(plugin).async(() -> {
            while (plugin.getClaimManager().getClaimAt(location) != null) {
                location.setX(location.getX() + 1);
                location.setZ(location.getZ() + 1);
            }

            new Scheduler(plugin).sync(() -> {
                location.setY(location.getBukkit().getWorld().getHighestBlockYAt(location.getBukkit().getBlockX(), location.getBukkit().getBlockZ()));
                player.teleport(location.getBukkit().add(0, 1.0, 0.0));
            }).run();
        }).run();
    }
}