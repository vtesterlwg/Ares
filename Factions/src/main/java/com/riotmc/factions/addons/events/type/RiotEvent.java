package com.riotmc.factions.addons.events.type;

import java.util.UUID;

public interface RiotEvent {
    UUID getOwnerId();

    String getName();

    String getDisplayName();
}
