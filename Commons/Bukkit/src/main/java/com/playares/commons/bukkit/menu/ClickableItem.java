package com.playares.commons.bukkit.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Represents a clickable item in a Menu
 */
@AllArgsConstructor
public final class ClickableItem {
    /**
     * Bukkit ItemStack
     */
    @Nonnull @Getter public final ItemStack item;

    /**
     * Menu slot position
     */
    @Getter
    public final int position;

    /**
     * Click result
     */
    @Getter
    public final ClickResult result;
}