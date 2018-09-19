package com.playares.commons.bukkit.menu;

import org.bukkit.event.inventory.ClickType;

/**
 * Represents a click in a Menu
 */
public interface ClickResult {
    /**
     * Fires when an item is clicked in a menu
     * @param type ClickType
     */
    void click(ClickType type);
}
