package com.playares.factions.claims.data;

import java.util.UUID;

public interface Claimable {
    UUID getUniqueId();

    UUID getOwnerId();
}
