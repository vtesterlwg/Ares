package com.playares.factions.addons.boosts;

import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.boosts.command.BoostCommand;
import com.playares.factions.addons.boosts.data.ActiveBoost;
import com.playares.factions.addons.boosts.listener.BoostListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

public final class BoostAddon implements Addon {
    @Getter public final Factions plugin;
    @Getter public final BoostHandler handler;
    @Getter @Setter public ActiveBoost activeBoost;
    @Getter @Setter public BukkitTask completionTask;

    public BoostAddon(Factions plugin) {
        this.plugin = plugin;
        this.handler = new BoostHandler(this);
        this.activeBoost = null;
    }

    @Override
    public String getName() {
        return "Boosts";
    }

    @Override
    public void prepare() {}

    @Override
    public void start() {
        plugin.registerListener(new BoostListener(this));
        plugin.registerCommand(new BoostCommand(this));
    }

    @Override
    public void stop() {
        if (completionTask != null && !completionTask.isCancelled()) {
            completionTask.cancel();
            completionTask = null;
        }
    }
}