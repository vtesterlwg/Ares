package com.riotmc.factions.addons.stats.holder;

import com.riotmc.factions.addons.stats.StatsAddon;

public interface StatisticHolder {
    int getKills();

    int getDeaths();

    int getMinorEventCaptures();

    int getMajorEventCaptures();

    void addKill();

    void addDeath();

    void addMinorEventCapture();

    void addMajorEventCapture();

    default int calculateELO(StatsAddon addon) {
        int elo = addon.getStartingElo();
        elo += getKills() * addon.getEloModifierKill();
        elo -= getDeaths() * addon.getEloModifierDeath();
        elo += getMinorEventCaptures() * addon.getEloModifierMinorCapture();
        elo += getMajorEventCaptures() * addon.getEloModifierMajorCapture();

        if (elo < 0) {
            elo = 0;
        }

        return elo;
    }
}
