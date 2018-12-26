package com.riotmc.factions.addons.autosave;

import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.Addon;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

public final class AutosaveAddon implements Addon {
    /** Owning plugin **/
    @Getter public final Factions plugin;
    /** Updater Task **/
    @Getter public BukkitTask task;
    /** True if auto-saves are enabled **/
    private boolean enabled;
    /** Interval that the autosave task should run at **/
    private int interval;

    public AutosaveAddon(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Autosave";
    }

    @Override
    public void prepare() {
        final YamlConfiguration config = plugin.getConfig("config");
        this.enabled = config.getBoolean("autosave.enabled");
        this.interval = config.getInt("autosave.interval");
    }

    @Override
    public void start() {
        this.task = new Scheduler(plugin).async(() -> {
            if (!enabled) {
                return;
            }

            Logger.print("Preparing to auto-save...");

            plugin.getFactionManager().saveFactions(true);
            plugin.getPlayerManager().savePlayers(true);
            plugin.getClaimManager().saveClaims(true);

            Logger.print("Autosave completed");
        }).repeat(interval * 20L, interval * 20L).run();
    }

    @Override
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
