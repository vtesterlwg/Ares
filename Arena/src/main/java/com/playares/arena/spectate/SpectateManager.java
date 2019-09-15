package com.playares.arena.spectate;

import com.playares.arena.Arenas;
import lombok.Getter;

public final class SpectateManager {
    @Getter public final Arenas plugin;
    @Getter public final SpectateHandler handler;

    public SpectateManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new SpectateHandler(this);
    }
}
