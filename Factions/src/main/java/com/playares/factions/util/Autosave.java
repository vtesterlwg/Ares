package com.playares.factions.util;

import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

public final class Autosave {
    @Getter
    public final Factions plugin;

    @Getter
    public BukkitTask task;

    public Autosave(Factions plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.task = new Scheduler(plugin).async(() -> {
            Logger.print("Preparing to auto-save...");
            plugin.getFactionManager().saveFactions(true);
            plugin.getPlayerManager().savePlayers(true);
            plugin.getClaimManager().saveClaims(true);
            Logger.print("Autosave completed");
        }).repeat(plugin.getFactionConfig().getAutosaveInterval() * 20L, plugin.getFactionConfig().getAutosaveInterval() * 20L).run();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}