package com.playares.arena.aftermatch;

import java.util.UUID;

public interface AftermatchReport {
    UUID getUniqueId();

    UUID getMatchId();

    int getHits();

    double getDamage();
}
