package com.playares.factions.addons.stats;

import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.stats.listener.StatisticListener;
import com.playares.factions.addons.stats.lore.TrackableItemListener;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class StatsAddon implements Addon {
    @Getter public final Factions plugin;
    @Getter public boolean enabled;
    private TrackableItemListener trackableListener;

    public StatsAddon(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Statistics";
    }

    @Override
    public void prepare() {
        final YamlConfiguration config = plugin.getConfig("config");
        this.enabled = config.getBoolean("stats.enabled");
    }

    @Override
    public void start() {
        trackableListener = new TrackableItemListener();
        plugin.registerListener(trackableListener);
        plugin.registerListener(new StatisticListener(this));
    }

    @Override
    public void stop() {
        BlockBreakEvent.getHandlerList().unregister(trackableListener);
        PlayerDeathEvent.getHandlerList().unregister(trackableListener);
    }
}