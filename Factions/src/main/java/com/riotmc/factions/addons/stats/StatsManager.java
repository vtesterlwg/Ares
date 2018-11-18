package com.riotmc.factions.addons.stats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.commons.base.promise.Promise;
import com.playares.commons.bukkit.util.Scheduler;
import com.riotmc.factions.Factions;
import com.riotmc.factions.factions.PlayerFaction;
import com.riotmc.factions.players.FactionPlayer;
import com.riotmc.factions.players.PlayerDAO;
import lombok.Getter;

import java.util.List;

public final class StatsManager {
    @Getter
    public final Factions plugin;

    @Getter
    public final StatsAddon addon;

    public StatsManager(Factions plugin, StatsAddon addon) {
        this.plugin = plugin;
        this.addon = addon;
    }

    public int getELO(FactionPlayer player) {
        return player.getStats().calculateELO(addon);
    }

    public int getELO(PlayerFaction faction) {
        return faction.getStats().calculateELO(addon);
    }

    public void getPlayerLeaderboard(Promise<ImmutableList<FactionPlayer>> promise) {
        new Scheduler(plugin).async(() -> {
            final List<FactionPlayer> players = Lists.newArrayList(PlayerDAO.getPlayers(plugin, plugin.getMongo()));


        }).run();
    }
}
