package com.playares.arena.stats;

import java.util.UUID;

public interface StatisticHolder {
    UUID getUniqueId();

    int getHits();

    double getDamage();

    void addHit();

    void addDamage(double amount);
}
