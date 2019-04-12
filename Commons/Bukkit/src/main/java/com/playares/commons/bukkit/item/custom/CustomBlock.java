package com.playares.commons.bukkit.item.custom;

import org.bukkit.entity.Player;

public interface CustomBlock {
    /**
     * Task to run when this custom block is placed
     * @param who Placing player
     * @return Runnable task
     */
    default Runnable getPlace(Player who) {
        return null;
    }

    /**
     * @return True if this block can be placed
     */
    default boolean isPlaceable() {
        return true;
    }
}
