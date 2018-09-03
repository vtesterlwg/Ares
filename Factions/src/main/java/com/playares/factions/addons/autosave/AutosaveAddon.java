package com.playares.factions.addons.autosave;

import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

public final class AutosaveAddon implements Addon {
    @Getter
    public final Factions plugin;

    @Getter
    public BukkitTask task;

    public AutosaveAddon(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Autosave";
    }

    @Override
    public void prepare() {}

    @Override
    public void start() {
        this.task = new Scheduler(plugin).async(() -> {
            Logger.print("Preparing to auto-save...");

            plugin.getFactionManager().saveFactions(true);
            plugin.getPlayerManager().savePlayers(true);
            plugin.getClaimManager().saveClaims(true);

            Logger.print("Autosave completed");
        }).repeat(plugin.getFactionConfig().getAutosaveInterval() * 20L, plugin.getFactionConfig().getAutosaveInterval() * 20L).run();
    }

    @Override
    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
