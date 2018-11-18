package com.riotmc.arena.loadout;

import com.riotmc.arena.Arenas;
import com.riotmc.arena.loadout.cont.ClassLoadout;
import com.riotmc.arena.loadout.cont.StandardLoadout;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.services.classes.ClassService;
import com.riotmc.services.classes.data.classes.*;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class LoadoutHandler {
    @Nonnull @Getter
    public final Arenas plugin;

    public LoadoutHandler(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    public void createStandardLoadout(@Nonnull ItemStack[] contents,
                                      @Nonnull ItemStack[] armor,
                                      @Nonnull String name,
                                      @Nonnull SimplePromise promise) {

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

    public void createClassLoadout(@Nonnull ItemStack[] contents,
                                   @Nonnull ItemStack[] armor,
                                   @Nonnull String name,
                                   @Nonnull String classType,
                                   @Nonnull SimplePromise promise) {

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

    public void deleteLoadout(@Nonnull String name, @Nonnull SimplePromise promise) {
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