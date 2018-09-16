package com.playares.factions.addons.dungeons;

import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import lombok.Getter;

public final class DungeonAddon implements Addon {
    @Getter
    public final Factions plugin;

    public DungeonAddon(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Dungeons";
    }

    @Override
    public void prepare() {}

    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }
}
