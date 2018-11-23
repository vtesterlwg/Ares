package com.riotmc.factions.addons.events;

import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.Addon;
import lombok.Getter;

public final class EventsAddon implements Addon {
    @Getter public final Factions plugin;
    @Getter public boolean enabled;

    public EventsAddon(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Events";
    }

    @Override
    public void prepare() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
