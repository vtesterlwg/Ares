package com.playares.arena.mode;

import com.playares.arena.loadout.Loadout;
import com.playares.commons.bukkit.util.Players;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface Mode {
    String getName();

    ItemStack getIcon();

    List<Loadout> getLoadouts();

    void setIcon(ItemStack icon);

    default boolean isConfigured() {
        return getName() != null && getIcon() != null && !getLoadouts().isEmpty();
    }

    default void giveBooks(Player who) {
        Players.resetHealth(who);

        who.getInventory().clear();
        who.getInventory().setArmorContents(null);

        for (int i = 0; i < getLoadouts().size(); i++) {
            final Loadout loadout = getLoadouts().get(i);
            who.getInventory().setItem(i, loadout.getAsBook());
        }
    }
}
