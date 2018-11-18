package com.riotmc.arena.loadout;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.serialize.InventorySerializer;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.loadout.cont.ClassLoadout;
import com.riotmc.arena.loadout.cont.StandardLoadout;
import com.riotmc.services.classes.ClassService;
import com.riotmc.services.classes.data.classes.*;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public final class LoadoutManager {
    @Getter
    public final Arenas plugin;

    @Getter
    public final Set<Loadout> loadouts;

    @Getter
    public final YamlConfiguration config;

    public LoadoutManager(Arenas plugin) {
        this.plugin = plugin;
        this.loadouts = Sets.newConcurrentHashSet();
        this.config = plugin.getConfig("loadouts");

        for (String loadoutName : config.getConfigurationSection("loadouts").getKeys(false)) {
            final ItemStack[] contents = InventorySerializer.decodeItemStacks(config.getString("loadouts." + loadoutName + ".contents"));
            final ItemStack[] armor = InventorySerializer.decodeItemStacks(config.getString("loadouts." + loadoutName + ".armor"));

            if (config.get("loadouts." + loadoutName + ".class-type") != null) {
                final ClassService classService = (ClassService)plugin.getService(ClassService.class);

                if (classService == null) {
                    Logger.error("Skipping loadout '" + loadoutName + "' because it is class-based and the service can not be found");
                    continue;
                }

                final String className = config.getString("loadouts." + loadoutName + ".class-type");
                AresClass type = null;

                if (className.equalsIgnoreCase("archer")) {
                    type = classService.getClass(ArcherClass.class);
                } else if (className.equalsIgnoreCase("rogue")) {
                    type = classService.getClass(RogueClass.class);
                } else if (className.equalsIgnoreCase("bard")) {
                    type = classService.getClass(BardClass.class);
                } else if (className.equalsIgnoreCase("diver")) {
                    type = classService.getClass(DiverClass.class);
                }

                if (type == null) {
                    Logger.error("Failed to find class type for loadout '" + loadoutName + "'");
                    continue;
                }

                final ClassLoadout classLoadout = new ClassLoadout(plugin, loadoutName, contents, armor, type);
                loadouts.add(classLoadout);
                continue;
            }

            final StandardLoadout loadout = new StandardLoadout(loadoutName, contents, armor);
            loadouts.add(loadout);
        }

        Logger.print("Loaded " + loadouts.size() + " Loadouts");
    }

    public Loadout getLoadout(String name) {
        return loadouts.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void saveLoadout(Loadout loadout) {
        config.set("loadouts." + loadout.getName() + ".contents", InventorySerializer.encodeItemStacksToString(loadout.getContents()));
        config.set("loadouts." + loadout.getName() + ".armor", InventorySerializer.encodeItemStacksToString(loadout.getArmor()));

        if (loadout instanceof ClassLoadout) {
            final ClassLoadout classLoadout = (ClassLoadout)loadout;
            final String represented = classLoadout.getClassType().getName().toLowerCase();

            config.set("loadouts." + loadout.getName() + ".class-type", represented);
        }

        plugin.saveConfig("loadouts", config);
    }

    public void deleteLoadout(Loadout loadout) {
        config.set("loadouts." + loadout.getName(), null);
        plugin.saveConfig("loadouts", config);
    }
}