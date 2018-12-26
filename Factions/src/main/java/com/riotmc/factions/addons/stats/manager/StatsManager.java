package com.riotmc.factions.addons.stats.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.riotmc.commons.base.promise.Promise;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.stats.StatsAddon;
import com.riotmc.factions.factions.data.PlayerFaction;
import com.riotmc.factions.players.dao.PlayerDAO;
import com.riotmc.factions.players.data.FactionPlayer;
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
