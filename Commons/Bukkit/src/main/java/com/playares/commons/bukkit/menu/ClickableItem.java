package com.playares.commons.bukkit.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class ClickableItem {
    @Nonnull @Getter public final ItemStack item;

    @Getter
    public final int position;

    @Getter
    public final ClickResult result;
}