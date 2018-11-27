package com.riotmc.factions.addons.events.type;

import com.riotmc.factions.factions.PlayerFaction;

import java.util.UUID;

public interface RiotEvent {
    UUID getOwnerId();

    String getName();

    String getDisplayName();

    void setName(String name);

    void setDisplayName(String displayName);

    void start();

    void cancel();

    void capture(PlayerFaction winner);
}
