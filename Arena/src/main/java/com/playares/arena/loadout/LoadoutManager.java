package com.playares.arena.loadout;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.loadout.cont.StandardLoadout;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.serialize.InventorySerializer;
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
        plugin.saveConfig("loadouts", config);
    }

    public void deleteLoadout(Loadout loadout) {
        config.set("loadouts." + loadout.getName(), null);
        plugin.saveConfig("loadouts", config);
    }
}