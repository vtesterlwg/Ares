package com.playares.arena.loadout;

import com.playares.arena.Arenas;
import com.playares.arena.loadout.cont.StandardLoadout;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public final class LoadoutHandler {
    @Getter
    public final Arenas plugin;

    public LoadoutHandler(Arenas plugin) {
        this.plugin = plugin;
    }

    public void createLoadout(ItemStack[] contents, ItemStack[] armor, String name, SimplePromise promise) {
        final Loadout existing = plugin.getLoadoutManager().getLoadout(name);

        if (existing != null) {
            promise.failure("Loadout name is already in use");
            return;
        }

        final StandardLoadout kit = new StandardLoadout(name, contents, armor);

        plugin.getLoadoutManager().getLoadouts().add(kit);
        plugin.getLoadoutManager().saveLoadout(kit);

        Logger.print("Created kit '" + name + "'");

        promise.success();
    }

    public void deleteLoadout(String name, SimplePromise promise) {
        final Loadout loadout = plugin.getLoadoutManager().getLoadout(name);

        if (loadout == null) {
            promise.failure("Loadout not found");
            return;
        }

        plugin.getLoadoutManager().getLoadouts().remove(loadout);
        plugin.getLoadoutManager().deleteLoadout(loadout);

        Logger.print("Deleted loadout '" + name + "'");

        promise.success();
    }
}