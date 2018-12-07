package com.riotmc.factions.addons.events;

import lombok.Getter;

public final class EventsManager {
    @Getter public final EventsAddon addon;
    @Getter public final EventsHandler handler;

    public EventsManager(EventsAddon addon) {
        this.addon = addon;
        this.handler = new EventsHandler(this);
    }
}
