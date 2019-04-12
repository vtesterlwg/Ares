package com.playares.factions.addons.events.builder.type;

import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.factions.data.ServerFaction;

import java.util.UUID;

public interface EventBuilder {
    UUID getBuilder();

    ServerFaction getOwningFaction();

    String getName();

    String getDisplayName();

    BLocatable getLootChest();

    void setOwningFaction(String name, FailablePromise<String> promise);

    void setName(String name, FailablePromise<String> promise);

    void setDisplayName(String name);

    void setLootChest(BLocatable location, FailablePromise<String> promise);
}
