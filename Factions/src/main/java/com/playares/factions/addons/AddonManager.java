package com.playares.factions.addons;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.addons.mining.MiningAddon;
import lombok.Getter;

import java.util.Set;

public final class AddonManager {
    @Getter
    public final Factions plugin;

    @Getter
    public final Set<Addon> addons;

    public AddonManager(Factions plugin) {
        this.plugin = plugin;
        this.addons = Sets.newHashSet();

        registerAddon(new MiningAddon(plugin));
    }

    public void startAddons() {
        Logger.print("Starting Faction Addons...");

        addons.forEach(addon -> {
            addon.prepare();
            addon.start();
        });

        Logger.print("Finished starting Faction Addons");
    }

    public void stopAddons() {
        Logger.print("Stopping Faction Addons...");
        addons.forEach(Addon::stop);
        addons.clear();
        Logger.print("Finished stopping Faction Addons");
    }

    public void registerAddon(Addon addon) {
        this.addons.add(addon);
    }
}