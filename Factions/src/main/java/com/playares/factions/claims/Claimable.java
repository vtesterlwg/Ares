package com.playares.factions.claims;

import java.util.UUID;

public interface Claimable {
    UUID getUniqueId();

    UUID getOwnerId();
}
