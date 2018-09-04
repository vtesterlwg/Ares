package com.playares.factions.addons;

import com.google.common.collect.Maps;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.addons.autosave.AutosaveAddon;
import com.playares.factions.addons.mining.MiningAddon;
import com.playares.factions.addons.stats.StatsAddon;
import lombok.Getter;

import java.util.Map;

public final class AddonManager {
    @Getter
    public final Factions plugin;

    @Getter
    public final Map<Class<? extends Addon>, Addon> addons;

    public AddonManager(Factions plugin) {
        this.plugin = plugin;
        this.addons = Maps.newHashMap();

        registerAddon(new MiningAddon(plugin));
        registerAddon(new StatsAddon(plugin));
        registerAddon(new AutosaveAddon(plugin));
    }

    public void startAddons() {
        Logger.print("Starting Faction Addons...");

        addons.values().forEach(addon -> {
            addon.prepare();
            addon.start();
            Logger.print("Started Factions Addon: " + addon.getName());
        });

        Logger.print("Finished starting Faction Addons");
    }

    public void stopAddons() {
        Logger.print("Stopping Faction Addons...");

        addons.values().forEach(addon -> {
            addon.stop();
            Logger.print("Stopped Factions Addon: " + addon.getName());
        });

        addons.clear();

        Logger.print("Finished stopping Faction Addons");
    }

    public void registerAddon(Addon addon) {
        this.addons.put(addon.getClass(), addon);
    }

    public Addon getAddon(Class<? extends Addon> clazz) {
        return addons.get(clazz);
    }
}