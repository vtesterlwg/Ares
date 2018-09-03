package com.playares.factions.addons.stats.holder;

public interface StatisticHolder {
    int getKills();

    int getDeaths();

    int getMinorEventCaptures();

    int getMajorEventCaptures();

    void addKill();

    void addDeath();

    void addMinorEventCapture();

    void addMajorEventCapture();
}
