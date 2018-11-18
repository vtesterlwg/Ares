package com.riotmc.arena.mode;

import com.riotmc.arena.Arenas;
import com.riotmc.arena.loadout.Loadout;
import com.riotmc.arena.mode.cont.StandardMode;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.logger.Logger;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class ModeHandler {
    @Getter
    public final Arenas plugin;

    public ModeHandler(Arenas plugin) {
        this.plugin = plugin;
    }

    public void createMode(@Nonnull String name, @Nonnull SimplePromise promise) {
        final Mode existing = plugin.getModeManager().getMode(name);

        if (existing != null) {
            promise.failure("Mode name is already in use");
            return;
        }

        final StandardMode mode = new StandardMode(name);

        plugin.getModeManager().getModes().add(mode);

        Logger.print("Mode '" + name + "' has been created");

        promise.success();
    }

    public void deleteMode(@Nonnull String name, @Nonnull SimplePromise promise) {
        final Mode mode = plugin.getModeManager().getMode(name);

        if (mode == null) {
            promise.failure("Mode not found");
            return;
        }

        plugin.getModeManager().getModes().remove(mode);
        plugin.getModeManager().deleteMode(mode);

        Logger.print("Mode '" + mode.getName() + "' has been deleted");

        promise.success();
    }

    public void setModeIcon(@Nonnull ItemStack item, @Nonnull String name, @Nonnull SimplePromise promise) {
        final Mode mode = plugin.getModeManager().getMode(name);

        if (mode == null) {
            promise.failure("Mode not found");
            return;
        }

        mode.setIcon(item);

        plugin.getModeManager().saveMode(mode);

        Logger.print("Updated icon for mode '" + name + "'");

        promise.success();
    }

    public void addLoadout(@Nonnull String modeName, @Nonnull String kitName, @Nonnull SimplePromise promise) {
        final Mode mode = plugin.getModeManager().getMode(modeName);
        final Loadout loadout = plugin.getLoadoutManager().getLoadout(kitName);

        if (mode == null) {
            promise.failure("Mode not found");
            return;
        }

        if (loadout == null) {
            promise.failure("Loadout not found");
            return;
        }

        if (mode.getLoadouts().contains(loadout)) {
            promise.failure("Mode already has this loadout applied");
            return;
        }

        mode.getLoadouts().add(loadout);

        plugin.getModeManager().saveMode(mode);

        Logger.print("Added loadout " + loadout.getName() + " to mode " + mode.getName());

        promise.success();
    }

    public void removeLoadout(@Nonnull String modeName, @Nonnull String kitName, @Nonnull SimplePromise promise) {
        final Mode mode = plugin.getModeManager().getMode(modeName);
        final Loadout loadout = plugin.getLoadoutManager().getLoadout(kitName);

        if (mode == null) {
            promise.failure("Mode not found");
            return;
        }

        if (loadout == null) {
            promise.failure("Loadout not found");
            return;
        }

        if (!mode.getLoadouts().contains(loadout)) {
            promise.failure("This mode does not have this loadout");
            return;
        }

        mode.getLoadouts().remove(loadout);

        plugin.getModeManager().saveMode(mode);

        Logger.print("Removed loadout '" + loadout.getName() + "' from mode '" + mode.getName() + "'");
        promise.success();
    }
}
