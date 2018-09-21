package com.playares.factions.factions;

import java.util.UUID;

public interface Faction {
    /** Faction Unique ID **/
    UUID getUniqueId();

    /** Faction Name **/
    String getName();

    /**
     * Sets the Faction Name
     * @param name Name
     */
    void setName(String name);
}
