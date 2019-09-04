package com.playares.factions.addons.stats;

import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.events.event.EventCaptureEvent;
import com.playares.factions.addons.stats.command.StatsCommand;
import com.playares.factions.addons.stats.handler.FactionStatsHandler;
import com.playares.factions.addons.stats.handler.PlayerStatsHandler;
import com.playares.factions.addons.stats.listener.StatisticListener;
import com.playares.factions.addons.stats.lore.TrackableItemListener;
import com.playares.services.playerclasses.event.ConsumeClassItemEvent;
import com.playares.services.playerclasses.event.RogueBackstabEvent;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class StatsAddon implements Addon {
    @Getter public final Factions plugin;
    @Getter public boolean enabled;
    @Getter public PlayerStatsHandler playerHandler;
    @Getter public FactionStatsHandler factionHandler;

    private TrackableItemListener trackableListener;
    private StatisticListener statisticListener;

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
        playerHandler = new PlayerStatsHandler(this);
        factionHandler = new FactionStatsHandler(this);

        getPlugin().registerCommand(new StatsCommand(this));

        trackableListener = new TrackableItemListener();
        plugin.registerListener(trackableListener);

        statisticListener = new StatisticListener(this);
        plugin.registerListener(statisticListener);
    }

    @Override
    public void stop() {
        BlockBreakEvent.getHandlerList().unregister(trackableListener);
        PlayerDeathEvent.getHandlerList().unregister(trackableListener);

        PlayerQuitEvent.getHandlerList().unregister(statisticListener);
        BlockBreakEvent.getHandlerList().unregister(statisticListener);
        EntityDeathEvent.getHandlerList().unregister(statisticListener);
        PlayerDeathEvent.getHandlerList().unregister(statisticListener);
        EventCaptureEvent.getHandlerList().unregister(statisticListener);
        ConsumeClassItemEvent.getHandlerList().unregister(statisticListener);
        PlayerDamagePlayerEvent.getHandlerList().unregister(statisticListener);
        RogueBackstabEvent.getHandlerList().unregister(statisticListener);
    }
}