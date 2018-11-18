package com.riotmc.factions.claims;

import java.util.UUID;

public interface Claimable {
    UUID getUniqueId();

    UUID getOwnerId();
}
