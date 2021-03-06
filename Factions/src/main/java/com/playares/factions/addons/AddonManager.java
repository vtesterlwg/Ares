package com.playares.factions.addons;

import com.google.common.collect.Maps;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.addons.autosave.AutosaveAddon;
import com.playares.factions.addons.boosts.BoostAddon;
import com.playares.factions.addons.crowbars.CrowbarAddon;
import com.playares.factions.addons.economy.EconomyAddon;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.loggers.LoggerAddon;
import com.playares.factions.addons.mining.MiningAddon;
import com.playares.factions.addons.spawnpoints.SpawnpointAddon;
import com.playares.factions.addons.staff.StaffAddon;
import com.playares.factions.addons.states.ServerStateAddon;
import com.playares.factions.addons.stats.StatsAddon;
import lombok.Getter;

import java.util.Map;

public final class AddonManager {
    @Getter public final Factions plugin;
    @Getter public final Map<Class<? extends Addon>, Addon> addons;

    public AddonManager(Factions plugin) {
        this.plugin = plugin;
        this.addons = Maps.newHashMap();

        registerAddon(new EconomyAddon(plugin));
        registerAddon(new ServerStateAddon(plugin));
        registerAddon(new EventsAddon(plugin));
        registerAddon(new MiningAddon(plugin));
        registerAddon(new StatsAddon(plugin));
        registerAddon(new AutosaveAddon(plugin));
        registerAddon(new LoggerAddon(plugin));
        registerAddon(new SpawnpointAddon(plugin));
        registerAddon(new CrowbarAddon(plugin));
        registerAddon(new BoostAddon(plugin));
        registerAddon(new StaffAddon(plugin));
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

    public void reloadAddons() {
        addons.values().forEach(addon -> {
            addon.prepare();
            Logger.print("Reloaded Factions Addon: " + addon.getName());
        });

        Logger.print("Finished reloading Faction Addons");
    }

    public void registerAddon(Addon addon) {
        this.addons.put(addon.getClass(), addon);
    }

    public Addon getAddon(Class<? extends Addon> clazz) {
        return addons.get(clazz);
    }
}