package com.playares.factions.factions;

import java.util.UUID;

public interface Faction {
    UUID getUniqueId();

    String getName();

    void setName(String name);
}
