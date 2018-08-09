package com.playares.commons.bukkit.item.custom;

import org.bukkit.entity.Player;

public interface CustomBlock {
    default Runnable getPlace(Player who) {
        return null;
    }

    default boolean isPlaceable() {
        return true;
    }
}
