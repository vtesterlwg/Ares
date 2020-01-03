package com.playares.civilization.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class CivUtils {
    public static void sendMessage(Collection<? extends Player> players, String message, Location origin, double range) {
        players.stream().filter(player -> player.getLocation().distance(origin) <= range).forEach(player -> player.sendMessage(message));
    }
}