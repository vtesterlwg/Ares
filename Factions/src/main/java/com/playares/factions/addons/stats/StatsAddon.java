package com.playares.factions.addons.stats;

import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.stats.holder.StatisticEventListener;
import com.playares.factions.addons.stats.lore.TrackableItemListener;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class StatsAddon implements Addon {
    @Getter public final Factions plugin;
    @Getter public StatsHandler statsHandler;
    @Getter public StatsManager statsManager;
    @Getter public boolean enabled;
    @Getter public int startingElo;
    @Getter public int eloModifierKill;
    @Getter public int eloModifierDeath;
    @Getter public int eloModifierMinorCapture;
    @Getter public int eloModifierMajorCapture;
    private TrackableItemListener trackableListener;
    private StatisticEventListener statsListener;

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
        this.startingElo = config.getInt("stats.starting-elo");
        this.eloModifierKill = config.getInt("stats.elo-modifiers.kill");
        this.eloModifierDeath = config.getInt("stats.elo-modifiers.death");
        this.eloModifierMinorCapture = config.getInt("stats.elo-modifiers.minor-capture");
        this.eloModifierMajorCapture = config.getInt("stats.elo-modifiers.major-capture");
    }

    @Override
    public void start() {
        statsHandler = new StatsHandler(plugin, this);
        statsManager = new StatsManager(plugin, this);
        trackableListener = new TrackableItemListener();
        statsListener = new StatisticEventListener(plugin);

        plugin.registerListener(trackableListener);
        plugin.registerListener(statsListener);

        plugin.registerCommand(new StatsCommand(statsHandler));
    }

    @Override
    public void stop() {
        BlockBreakEvent.getHandlerList().unregister(statsListener);
        PlayerDeathEvent.getHandlerList().unregister(statsListener);
        PlayerQuitEvent.getHandlerList().unregister(statsListener);
        BlockBreakEvent.getHandlerList().unregister(trackableListener);
        PlayerDeathEvent.getHandlerList().unregister(trackableListener);
    }
}