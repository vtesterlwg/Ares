package com.playares.arena.loadout;

import com.playares.arena.Arenas;
import com.playares.arena.loadout.cont.ClassLoadout;
import com.playares.arena.loadout.cont.StandardLoadout;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.classes.ClassService;
import com.playares.services.classes.data.classes.*;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public final class LoadoutHandler {
    @Getter
    public final Arenas plugin;

    public LoadoutHandler(Arenas plugin) {
        this.plugin = plugin;
    }

    public void createStandardLoadout(ItemStack[] contents, ItemStack[] armor, String name, SimplePromise promise) {
        final Loadout existing = plugin.getLoadoutManager().getLoadout(name);

        if (existing != null) {
            promise.failure("Loadout name is already in use");
            return;
        }

        final StandardLoadout kit = new StandardLoadout(name, contents, armor);

        plugin.getLoadoutManager().getLoadouts().add(kit);
        plugin.getLoadoutManager().saveLoadout(kit);

        Logger.print("Created loadout '" + name + "'");

        promise.success();
    }

    public void createClassLoadout(ItemStack[] contents, ItemStack[] armor, String name, String classType, SimplePromise promise) {
        final ClassService classService = (ClassService)plugin.getService(ClassService.class);
        final Loadout existing = plugin.getLoadoutManager().getLoadout(name);
        AresClass type = null;

        if (classService == null) {
            promise.failure("Failed to obtain Class Service");
            return;
        }

        if (existing != null) {
            promise.failure("Loadout name is already in use");
            return;
        }

        if (classType.equalsIgnoreCase("archer")) {
            type = classService.getClass(ArcherClass.class);
        } else if (classType.equalsIgnoreCase("rogue")) {
            type = classService.getClass(RogueClass.class);
        } else if (classType.equalsIgnoreCase("bard")) {
            type = classService.getClass(BardClass.class);
        } else if (classType.equalsIgnoreCase("diver")) {
            type = classService.getClass(DiverClass.class);
        }

        if (type == null) {
            promise.failure("Invalid class type");
            return;
        }

        final ClassLoadout kit = new ClassLoadout(plugin, name, contents, armor, type);

        plugin.getLoadoutManager().getLoadouts().add(kit);
        plugin.getLoadoutManager().saveLoadout(kit);

        Logger.print("Created loadout '" + name + "'");

        promise.success();
    }

    public void deleteLoadout(String name, SimplePromise promise) {
        final Loadout loadout = plugin.getLoadoutManager().getLoadout(name);

        if (loadout == null) {
            promise.failure("Loadout not found");
            return;
        }

        plugin.getModeManager().getModes().stream().filter(mode -> mode.getLoadouts().contains(loadout)).forEach(mode -> {
            mode.getLoadouts().remove(loadout);
            plugin.getModeManager().saveMode(mode);
        });

        plugin.getLoadoutManager().getLoadouts().remove(loadout);
        plugin.getLoadoutManager().deleteLoadout(loadout);

        Logger.print("Deleted loadout '" + name + "'");

        promise.success();
    }
}