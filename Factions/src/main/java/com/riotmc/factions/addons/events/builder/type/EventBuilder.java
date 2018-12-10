package com.riotmc.factions.addons.events.builder.type;

import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.factions.factions.ServerFaction;

import java.util.UUID;

public interface EventBuilder {
    UUID getBuilder();

    ServerFaction getOwningFaction();

    String getName();

    String getDisplayName();

    void setOwningFaction(String name, FailablePromise<String> promise);

    void setName(String name, FailablePromise<String> promise);

    void setDisplayName(String name);
}
