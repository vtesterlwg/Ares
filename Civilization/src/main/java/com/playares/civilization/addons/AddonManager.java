package com.playares.civilization.addons;

import com.google.common.collect.Maps;
import com.playares.civilization.Civilizations;
import com.playares.civilization.addons.prisonpearls.PrisonPearlAddon;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;

import java.util.Map;

public final class AddonManager {
    @Getter public final Civilizations plugin;
    @Getter public final Map<Class<? extends CivAddon>, CivAddon> addons;

    public AddonManager(Civilizations plugin) {
        this.plugin = plugin;
        this.addons = Maps.newHashMap();

        register(new PrisonPearlAddon(this));
    }

    /**
     * Start all addons
     */
    public void start() {
        addons.values().forEach(addon -> {
            addon.prepare();
            addon.start();
            Logger.print("Started Addon: " + addon.getName());
        });

        Logger.print("Finished starting Civilization Addons");
    }

    /**
     * Stop all addons
     */
    public void stop() {
        addons.values().forEach(addon -> {
            addon.stop();
            Logger.print("Stopped Addon: " + addon.getName());
        });

        Logger.print("Finished stopping Civilization Addons");
    }

    /**
     * Reload configurations for all addons
     */
    public void reload() {
        Logger.warn("Reloading all Civilization Addons");

        addons.values().forEach(addon -> {
            addon.prepare();
            Logger.print("Reloaded Addon: " + addon.getName());
        });

        Logger.print("Finished reloading Civilization Addons");
    }

    /**
     * Add a new CivAddon to the addon registry
     * @param addon CivAddon
     */
    private void register(CivAddon addon) {
        this.addons.put(addon.getClass(), addon);
    }

    /**
     * Obtain a CivAddon from the existing registry
     * @param clazz Class
     * @return CivAddon
     */
    public CivAddon getAddon(Class<? extends CivAddon> clazz) {
        return addons.get(clazz);
    }
}