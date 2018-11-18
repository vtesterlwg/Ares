package com.riotmc.arena.mode;

import com.playares.commons.bukkit.util.Players;
import com.riotmc.arena.loadout.Loadout;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface Mode {
    @Nonnull
    String getName();

    @Nullable
    ItemStack getIcon();

    @Nonnull
    List<Loadout> getLoadouts();

    void setIcon(@Nonnull ItemStack icon);

    default boolean isConfigured() {
        return getIcon() != null && !getLoadouts().isEmpty();
    }

    default void giveBooks(@Nonnull Player who) {
        Players.resetHealth(who);

        who.getInventory().clear();
        who.getInventory().setArmorContents(null);

        for (int i = 0; i < getLoadouts().size(); i++) {
            final Loadout loadout = getLoadouts().get(i);
            who.getInventory().setItem(i, loadout.getAsBook());
        }
    }
}